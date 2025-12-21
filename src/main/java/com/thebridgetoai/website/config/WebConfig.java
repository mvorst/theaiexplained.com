package com.thebridgetoai.website.config;

import java.io.File;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattvorst.shared.util.UUIDConverter;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

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

	@Bean
	public ViewResolver jspViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/jsp/");
		resolver.setSuffix(".jsp");
		resolver.setOrder(1);
		return resolver;
	}

	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
		return factory -> {
			// Set document base to webapp directory for JSP support when running embedded
			File webappDir = new File("webapp");
//			File webappDir = new File("src/main/webapp");
			if (webappDir.exists()) {
				factory.setDocumentRoot(webappDir);
			}
		};
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
