package com.spendsmart.budget.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping("/send-budget-alert")
    void sendBudgetAlert(
            @RequestParam("userId") Long userId,
            @RequestParam("message") String message,
            @RequestParam("amount") Double amount
    );
}
