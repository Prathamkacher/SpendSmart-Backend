// com/spendsmart/auth/AuthServiceApplication.java
package com.spendsmart.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Authentication Service.
 * Provides user management, security tokens, and administrative controls.
 */
@SpringBootApplication
@EnableScheduling
@EnableFeignClients
@EnableAsync
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}