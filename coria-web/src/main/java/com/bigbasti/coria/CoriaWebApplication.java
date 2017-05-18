package com.bigbasti.coria;

import com.bigbasti.coria.config.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan(basePackages = "com.bigbasti.coria")
public class CoriaWebApplication {

	@Autowired
	Environment env;

	@Bean
	AppContext createAppContext(){
		AppContext context = AppContext.getInstance();
		String dbProvider = env.getProperty("coria.database");
		context.setDatabaseProvider(dbProvider);
		return context;
	}

	public static void main(String[] args) {
		SpringApplication.run(CoriaWebApplication.class, args);
	}
}
