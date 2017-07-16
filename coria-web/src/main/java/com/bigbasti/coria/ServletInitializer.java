package com.bigbasti.coria;

import com.bigbasti.coria.config.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@SpringBootApplication
@ComponentScan(basePackages = "com.bigbasti.coria")
public class ServletInitializer extends SpringBootServletInitializer {

	@Autowired
	Environment env;

	@Bean
	AppContext createAppContext(){
		AppContext context = AppContext.getInstance();
		String dbProvider = env.getProperty("coria.database");
		String workingDir = env.getProperty("coria.workingdirectory");
		context.setDatabaseProvider(dbProvider);
		context.setWorkingDirectory(workingDir);
		return context;
	}

	@Bean
	public ThreadPoolTaskExecutor getThreadPoolTaskExecutor(){
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("metric-");
		executor.initialize();
		return executor;
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ServletInitializer.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ServletInitializer.class, args);
	}

}
