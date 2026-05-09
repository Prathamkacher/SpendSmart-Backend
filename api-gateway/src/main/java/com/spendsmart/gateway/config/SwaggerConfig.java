package com.spendsmart.gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerConfig {

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();
        
        // Register Auth Service
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "Auth Service", 
                "/auth-docs/v3/api-docs", 
                "Auth Service"
        ));
        
        // Register Expense Service
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                "Expense Service", 
                "/expense-docs/v3/api-docs", 
                "Expense Service"
        ));
        
        properties.setUrls(urls);
        return properties;
    }
}
