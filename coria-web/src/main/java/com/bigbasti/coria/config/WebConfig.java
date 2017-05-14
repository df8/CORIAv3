package com.bigbasti.coria.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Created by Sebastian Gross
 */
@Component
public class WebConfig extends WebMvcConfigurerAdapter {
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/").setViewName("forward:/index.html");
//    }

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

        super.addResourceHandlers(registry);
    }
}
