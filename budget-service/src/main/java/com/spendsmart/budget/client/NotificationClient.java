package com.spendsmart.budget.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for communicating with the Notification service.
 * Used to trigger budget-related alerts and spending notifications.
 */
@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping("/send-budget-alert")
    void sendBudgetAlert(
            @RequestParam("userId") Long userId,
            @RequestParam("message") String message,
            @RequestParam("amount") Double amount
    );
}
