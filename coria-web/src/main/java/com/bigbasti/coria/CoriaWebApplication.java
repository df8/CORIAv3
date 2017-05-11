package com.bigbasti.coria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.bigbasti.coria")
public class CoriaWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoriaWebApplication.class, args);
	}
}
