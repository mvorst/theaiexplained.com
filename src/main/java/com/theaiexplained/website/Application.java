package com.theaiexplained.website;

import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.theaiexplained", "com.mattvorst"})
public class Application {

	public static void main(String[] args) {

		String environment = null;
		if(args.length > 0 && !Utils.empty(args[0])){
			environment = args[0];
		}

		Environment.instance(environment);

		SpringApplication.run(Application.class, args);
	}

}
