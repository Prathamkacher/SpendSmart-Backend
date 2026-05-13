package com.spendsmart.income;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Income Service.
 * Manages user income records and provides financial tracking capabilities.
 */
@SpringBootApplication
@EnableFeignClients
public class IncomeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IncomeServiceApplication.class, args);
    }
}
