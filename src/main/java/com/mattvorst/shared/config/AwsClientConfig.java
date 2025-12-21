package com.mattvorst.shared.config;

import com.mattvorst.shared.service.AmazonServiceFactory;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsClientConfig {
	private static final Logger log = LoggerFactory.getLogger(AwsClientConfig.class);

	@PreDestroy
	public void shutdown() {
		log.info("Shutting down AWS SDK HTTP clients");
		AmazonServiceFactory.shutdown();
	}
}
