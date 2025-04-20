package com.mattvorst.shared.logging.spring;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.service.AmazonServiceFactory;
import com.mattvorst.shared.util.Environment;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Component
public class SpringRequestInterceptor implements HandlerInterceptor {
	private static final Logger log = LoggerFactory.getLogger(SpringRequestInterceptor.class);

	@Autowired
	private JwtDecoder jwtDecoder;

	private SqsAsyncClient sqsAsyncClient;
	private String restRequestQueue;

	public SpringRequestInterceptor() {
		sqsAsyncClient = AmazonServiceFactory.getSqsAsyncClient(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
		restRequestQueue = Environment.get(EnvironmentConstants.AWS_SQS_REST_REQUEST_QUEUE);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		SpringRequestHandler.handlePreRequest(request, response, sqsAsyncClient, restRequestQueue, jwtDecoder);

		return HandlerInterceptor.super.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception {
		HandlerInterceptor.super.afterCompletion(request, response, handler, exception);

		SpringRequestHandler.handlePostRequest(request, response, sqsAsyncClient, restRequestQueue, jwtDecoder);
	}
}
