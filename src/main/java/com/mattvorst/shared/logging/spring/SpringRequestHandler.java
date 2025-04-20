package com.mattvorst.shared.logging.spring;

import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.constant.EnvironmentType;
import com.mattvorst.shared.logging.constant.LogType;
import com.mattvorst.shared.security.token.AuthToken;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.LocaleThreadLocal;
import com.mattvorst.shared.util.LocaleUtils;
import com.mattvorst.shared.util.RequestThreadLocal;
import com.mattvorst.shared.util.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SpringRequestHandler {
	private static final Logger log = LoggerFactory.getLogger(SpringRequestHandler.class);

	public static void handlePreRequest(HttpServletRequest request, HttpServletResponse response, SqsAsyncClient sqsAsyncClient, String restRequestQueue, JwtDecoder jwtDecoder) {
		String requestId = request.getRemoteAddr() + "_" + UUID.randomUUID() + "_" + System.currentTimeMillis();
		RequestThreadLocal.set(requestId);

		Locale locale = request.getLocale();
		if(!Utils.empty(request.getQueryString())){
			String[] keyValuePairArray = request.getQueryString().split("&");
			if(keyValuePairArray.length > 0) {
				for (String keyValuePair : keyValuePairArray) {
					String[] keyValue = keyValuePair.split("=");
					if(keyValue.length == 2){
						if("locale".equalsIgnoreCase(keyValue[0])) {
							locale = LocaleUtils.safeToLocale(keyValue[1]);
						}
					}
				}
			}
		}
		LocaleThreadLocal.set(locale);

		teardownLoggingContext();

		try {
			String publicIPAddress = request.getRemoteAddr();

			Map<String, Object> requestMap = new TreeMap<>();

			requestMap.put("logType", LogType.REQUEST.toString());
			requestMap.put("requestTime", Utils.toUtcTimestamp(new Date()));
			requestMap.put("requestId", requestId);
			requestMap.put("method", request.getMethod());
			requestMap.put("requestUri", request.getRequestURI());
			requestMap.put("queryString", request.getQueryString());

			Map<String, String> headerMap = new TreeMap<>();

			Enumeration<String> headerNameEnum = request.getHeaderNames();
			while (headerNameEnum.hasMoreElements()) {
				String headerName = headerNameEnum.nextElement();

				if ("Authorization".equalsIgnoreCase(headerName)) {
					addTokenClaimsToMap(requestMap);
				} else if ("source".equalsIgnoreCase(headerName)) {
					requestMap.put("source", request.getHeader(headerName));
				} else {
					headerMap.put(headerName.toLowerCase(), request.getHeader(headerName));
				}

				if ("X-Forwarded-For".equalsIgnoreCase(headerName)) {
					publicIPAddress = request.getHeader(headerName);
				} else {
					if (EnvironmentType.DEV.equals(Environment.getEnvironmentType())) {
						publicIPAddress = "170.249.17.176";
					}
				}

				requestMap.put("remoteAddress", publicIPAddress);
				request.setAttribute("remoteAddress", publicIPAddress);

				if ("user-agent".equalsIgnoreCase(headerName) && "ELB-HealthChecker/2.0".equalsIgnoreCase(request.getHeader(headerName))) {
					requestMap.put("source", "load-balancer");
					response.addHeader("source", "load-balancer");
				}
			}

			requestMap.put("headers", headerMap);

			request.setAttribute("servletRequestId", requestId);

			log.info(request.getMethod() + ":" + request.getRequestURI());

			if(Environment.getBoolean(EnvironmentConstants.LOG_REQUESTS_LOCALLY)) {
				log.debug(Utils.gson().toJson(requestMap));
			}else{
				sqsAsyncClient.sendMessage(SendMessageRequest.builder().queueUrl(restRequestQueue).messageBody(Utils.gson().toJson(requestMap)).build());
			}
		} catch (Throwable t) {
			log.error("REQUEST Exception", t);
		}

		setupLoggingContext();
	}

	public static void handlePostRequest(HttpServletRequest request, HttpServletResponse response, SqsAsyncClient sqsAsyncClient, String restRequestQueue, JwtDecoder jwtDecoder) {
		try {
			Map<String, Object> requestMap = new TreeMap<>();

			requestMap.put("logType", LogType.RESPONSE_COMPLETE.toString());
			requestMap.put("responseTime", Utils.toUtcTimestamp(new Date()));
			requestMap.put("requestId", RequestThreadLocal.get());
			requestMap.put("statusCode", Integer.toString(response.getStatus()));
			requestMap.put("requestUri", request.getRequestURI());

			Map<String, String> headerMap = new TreeMap<>();

			// Request Headers
			Enumeration<String> headerNameEnum = request.getHeaderNames();
			while (headerNameEnum.hasMoreElements()) {
				String headerName = headerNameEnum.nextElement();
				if ("Authorization".equalsIgnoreCase(headerName)) {
					addTokenClaimsToMap(requestMap);
				} else if ("source".equalsIgnoreCase(headerName)) {
					requestMap.put("source", request.getHeader(headerName));
				}

				if ("user-agent".equalsIgnoreCase(headerName) && "ELB-HealthChecker/2.0".equalsIgnoreCase(request.getHeader(headerName))) {
					requestMap.put("source", "load-balancer");
					response.addHeader("source", "load-balancer");
				}
			}

			for (String headerName : response.getHeaderNames()) {
				if ("Authorization".equalsIgnoreCase(headerName)) {
					requestMap.put("Authorization", response.getHeader(headerName));
				} else if ("source".equalsIgnoreCase(headerName)) {
					requestMap.put("source", response.getHeader(headerName));
				} else {
					headerMap.put(headerName.toLowerCase(), response.getHeader(headerName));
				}
			}

			requestMap.put("headers", headerMap);

			String message = Utils.gson().toJson(requestMap);

			if(Environment.getBoolean(EnvironmentConstants.LOG_REQUESTS_LOCALLY)) {
				log.debug(message);
			}else {
				sqsAsyncClient.sendMessage(SendMessageRequest.builder().queueUrl(restRequestQueue).messageBody(message).build());
			}
		} catch (Throwable t) {
			log.error("REQUEST_COMPLETE Exception", t);
		} finally {
			teardownLoggingContext();
		}
	}

	private static void setupLoggingContext() {
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() instanceof AuthToken) {
			AuthToken authToken = (AuthToken) SecurityContextHolder.getContext().getAuthentication();
			MDC.put("subject", authToken.getSubject());
		}

		if (RequestThreadLocal.get() != null) {
			MDC.put("requestId", RequestThreadLocal.get());
		}
	}

	private static void teardownLoggingContext() {
		MDC.clear();
	}

	public static void handleAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, SqsAsyncClient sqsAsyncClient, String restRequestQueue,
												   JwtDecoder jwtDecoder) {
		handlePreRequest(request, response, sqsAsyncClient, restRequestQueue, jwtDecoder);
		handlePostRequest(request, response, sqsAsyncClient, restRequestQueue, jwtDecoder);
	}

	public static void addTokenClaimsToMap(Map<String, Object> requestMap) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AuthToken) {
			AuthToken authToken = (AuthToken) authentication;

			requestMap.put("subjectName", authToken.getName());
			requestMap.put("subject", authToken.getSubject());
			requestMap.put("subjectUuid", authToken.getSubjectUuid());
		}
	}
}
