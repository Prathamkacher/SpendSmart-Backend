package com.spendsmart.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Budget Service.
 * Manages user budgets, category limits, and spending alerts.
 */
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class BudgetServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BudgetServiceApplication.class, args);
    }
}
