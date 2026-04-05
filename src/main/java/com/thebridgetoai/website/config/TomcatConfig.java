package com.thebridgetoai.website.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

	@Value("${tomcat.ajp.port:8009}")
	private int ajpPort;

	@Value("${tomcat.ajp.secret:changeit}")
	private String ajpSecret;

	@Bean
	public TomcatServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addAdditionalConnectors(ajpConnector());
		return tomcat;
	}

	private Connector ajpConnector() {
		Connector connector = new Connector("AJP/1.3");
		connector.setPort(ajpPort);
		connector.setSecure(false);
		connector.setAllowTrace(false);
		connector.setScheme("http");

		AbstractAjpProtocol<?> protocol =
			(AbstractAjpProtocol<?>) connector.getProtocolHandler();
		protocol.setSecret(ajpSecret);
		protocol.setSecretRequired(true);

		return connector;
	}
}
