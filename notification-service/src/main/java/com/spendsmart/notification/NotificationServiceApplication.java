package com.spendsmart.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Notification Service.
 * Manages user notifications, email alerts, and real-time messaging for the SpendSmart platform.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@org.springframework.scheduling.annotation.EnableAsync
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
