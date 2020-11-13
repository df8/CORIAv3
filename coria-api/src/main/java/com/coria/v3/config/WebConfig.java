package com.coria.v3.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.util.LinkedHashMap;

/**
 * Created by Sebastian Gross, 2017
 * Modified by David Fradin, 2020: Added support for GraphQL
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/").setViewName("forward:/index.html");
//    }

    /**
     * Source: https://github.com/graphql-java-kickstart/graphql-spring-boot/blob/master/graphql-spring-boot-autoconfigure/src/main/java/graphql/kickstart/spring/web/boot/GraphQLWebAutoConfiguration.java
     *
     * @return
     */
    @Bean
    @ConditionalOnClass({CorsFilter.class})
    public CorsFilter corsConfigurer() {
        LinkedHashMap<String, CorsConfiguration> corsConfigurations = new LinkedHashMap<>(1);
        CorsConfiguration corsConfiguration = (new CorsConfiguration())
                .applyPermitDefaultValues();
        corsConfiguration.addAllowedMethod("PUT");
        corsConfigurations.put("/**", corsConfiguration);
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.setCorsConfigurations(corsConfigurations);
        configurationSource.setAlwaysUseFullPath(true);
        return new CorsFilter(configurationSource);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedOrigins("*")
                .allowedHeaders("*");
        //This is not secure, but helpful in development environment
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Including all static resources.

        registry.addResourceHandler("/assets/**",
                "/public/css/**",
                "/public/img/**",
                "/public/js/**"
        ).addResourceLocations("/assets/",
                "/public/css/",
                "/public/img/",
                "/public/js/"
        ).resourceChain(true)
                .addResolver(new PathResourceResolver());
    }
}
