package com.thebridgetoai.website;

import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.thebridgetoai", "com.mattvorst"})
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {

		String environment = null;
		if(args.length > 0 && !Utils.empty(args[0])){
			environment = args[0];
		}

		Environment.instance(environment);

		SpringApplication.run(Application.class, args);
	}

}
