package com.theaiexplained.website.config;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattvorst.shared.async.processor.TaskProcessor;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.dao.FileDao;
import com.mattvorst.shared.dao.SecurityDao;
import com.mattvorst.shared.dao.SystemDao;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.UUIDConverter;
import com.theaiexplained.website.async.processor.AppTaskProcessor;
import com.theaiexplained.website.dao.ContentDao;
import com.theaiexplained.website.dao.UserDao;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

//	@Autowired
//	private SpringRequestInterceptor springRequestInterceptor;

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

		messageSource.setDefaultLocale(Locale.ENGLISH);
		messageSource.setBasenames("i18n/messages");

		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setDefaultEncoding("UTF-8");

		return messageSource;
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/jsp/", ".jsp");
	}

	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new UUIDConverter());
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper().setSerializationInclusion(Include.NON_NULL).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	//	@Bean
//	public ContentDao contentDao() {
//		return new ContentDao(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
//	}
//
//	@Bean
//	public UserDao userDao() {
//		return  new UserDao(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
//	}
//
//	@Bean
//	public FileDao fileDao() {
//		return new FileDao(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
//	}
//
//	@Bean
//	public SecurityDao securityDao() {
//		return new SecurityDao(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
//	}
//
//	@Bean
//	public SystemDao systemDao() {
//		return new SystemDao(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
//	}
}
