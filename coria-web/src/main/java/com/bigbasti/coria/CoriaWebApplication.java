package com.bigbasti.coria;

import com.bigbasti.coria.config.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@SpringBootApplication
@ComponentScan(basePackages = "com.bigbasti.coria")
public class CoriaWebApplication extends AsyncConfigurerSupport {

	@Autowired
	Environment env;

	@Bean
	AppContext createAppContext(){
		AppContext context = AppContext.getInstance();
		String dbProvider = env.getProperty("coria.database");
		context.setDatabaseProvider(dbProvider);
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
	public Executor getAsyncExecutor() {
		return getThreadPoolTaskExecutor();
	}

	public static void main(String[] args) {
		SpringApplication.run(CoriaWebApplication.class, args);
	}
}
