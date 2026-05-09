package com.spendsmart.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<Map<String, String>> authFallback() {
        return createFallbackResponse("Authentication Service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/expense")
    public Mono<Map<String, String>> expenseFallback() {
        return createFallbackResponse("Expense Service is currently down. Your request will be processed once it is back online.");
    }

    @GetMapping("/income")
    public Mono<Map<String, String>> incomeFallback() {
        return createFallbackResponse("Income Service is currently down. Please try again later.");
    }

    @GetMapping("/category")
    public Mono<Map<String, String>> categoryFallback() {
        return createFallbackResponse("Category Service is currently unavailable.");
    }

    @GetMapping("/budget")
    public Mono<Map<String, String>> budgetFallback() {
        return createFallbackResponse("Budget Service is currently unavailable.");
    }

    @GetMapping("/analytics")
    public Mono<Map<String, String>> analyticsFallback() {
        return createFallbackResponse("Analytics Service is currently unavailable. Reports cannot be generated at this moment.");
    }

    @GetMapping("/recurring")
    public Mono<Map<String, String>> recurringFallback() {
        return createFallbackResponse("Recurring Transaction Service is currently unavailable.");
    }

    @GetMapping("/notification")
    public Mono<Map<String, String>> notificationFallback() {
        return createFallbackResponse("Notification Service is currently unavailable. Notifications may be delayed.");
    }

    @GetMapping("/payment")
    public Mono<Map<String, String>> paymentFallback() {
        return createFallbackResponse("Payment Service is currently down. Transactions cannot be processed at this time.");
    }

    private Mono<Map<String, String>> createFallbackResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", message);
        return Mono.just(response);
    }
}
