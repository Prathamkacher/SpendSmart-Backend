package com.spendsmart.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

/**
 * Feign client for communicating with the Notification service.
 * Used to send system alerts based on analytical insights (e.g., budget warnings).
 */
@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping("/send")
    void sendNotification(@RequestBody Map<String, Object> request);
}
