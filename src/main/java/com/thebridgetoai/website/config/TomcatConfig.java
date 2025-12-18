package com.thebridgetoai.website.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

	@Value( "${server.tomcat.ajp.enabled}" )
	private boolean tomcatAJPEnabled;

	@Value( "${server.tomcat.ajp.port}" )
	private int tomcatAJPPort;

	@Value( "${server.tomcat.ajp.secret}" )
	private String tomcatAJPSecret;

	@Value( "${server.tomcat.ajp.protocol}" )
	private String tomcatAJPProtocol;

	@Bean
	public TomcatServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		if(tomcatAJPEnabled) {
			tomcat.addAdditionalConnectors(ajpConnector());
		}
		return tomcat;
	}

	private Connector ajpConnector() {
		Connector connector = new Connector(tomcatAJPProtocol);
		connector.setPort(tomcatAJPPort);
		connector.setSecure(true);
		connector.setAllowTrace(false);
		AjpNioProtocol protocol = (AjpNioProtocol)connector.getProtocolHandler();
		protocol.setSecret(tomcatAJPSecret);

		return connector;
	}
}
