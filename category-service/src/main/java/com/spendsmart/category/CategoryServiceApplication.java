package com.spendsmart.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main entry point for the Category microservice.
 * This service manages user-defined and default categories for income and expenses.
 */
@SpringBootApplication
@EnableFeignClients
public class CategoryServiceApplication {
    /**
     * Bootstraps the Spring Boot application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceApplication.class, args);
    }
}
