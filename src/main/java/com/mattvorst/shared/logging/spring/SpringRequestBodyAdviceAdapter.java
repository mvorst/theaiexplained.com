package com.mattvorst.shared.logging.spring;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.logging.constant.LogType;
import com.mattvorst.shared.security.SecureUriUtils;
import com.mattvorst.shared.service.AmazonServiceFactory;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.RequestThreadLocal;
import com.mattvorst.shared.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@ControllerAdvice
public class SpringRequestBodyAdviceAdapter extends RequestBodyAdviceAdapter {

	private static final Logger log = LoggerFactory.getLogger(SpringRequestBodyAdviceAdapter.class);

	private SqsAsyncClient sqsAsyncClient;
	private String restRequestQueue;

	public SpringRequestBodyAdviceAdapter() {
		sqsAsyncClient = AmazonServiceFactory.getSqsAsyncClient(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
		restRequestQueue = Environment.get(EnvironmentConstants.AWS_SQS_REST_REQUEST_QUEUE);
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {

		if(body != null){
			Map<String, Object> requestMap = new TreeMap<>();

			try{
				requestMap.put("logType", LogType.REQUEST_BODY.toString());
				requestMap.put("requestTime", Utils.toUtcTimestamp(new Date()));
				requestMap.put("requestId", RequestThreadLocal.get());

				if(targetType != null && !SecureUriUtils.isSecureObject(targetType)) {
					requestMap.put("requestBody", body);
				}else{
					log.debug("SECURE_TARGET: " + targetType.getTypeName());
				}

				Map<String, String> headerMap = new TreeMap<>();

				if(inputMessage.getHeaders() != null) {
					inputMessage.getHeaders().forEach((headerName, values) -> {
						String value = values != null && !values.isEmpty() ? values.get(0) : null;
						if ("Authorization".equalsIgnoreCase(headerName)) {
							SpringRequestHandler.addTokenClaimsToMap(requestMap);
						} else if ("source".equalsIgnoreCase(headerName)) {
							requestMap.put("source", value);
						} else{
							headerMap.put(headerName.toLowerCase(), value);

							if("user-agent".equalsIgnoreCase(headerName) && "ELB-HealthChecker/2.0".equalsIgnoreCase(value)) {
								requestMap.put("source", "load-balancer");
							}
						}
					});
					requestMap.put("headers", headerMap);
				}

				sqsAsyncClient.sendMessage(SendMessageRequest.builder()
						.queueUrl(restRequestQueue)
						.messageBody(Utils.gson().toJson(requestMap))
						.build());

			}catch(Throwable t){
				log.error("REQUEST_BODY Exception", t);
			}
		}

		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
	}

	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return super.handleEmptyBody(body, inputMessage, parameter, targetType, converterType);
	}
}
