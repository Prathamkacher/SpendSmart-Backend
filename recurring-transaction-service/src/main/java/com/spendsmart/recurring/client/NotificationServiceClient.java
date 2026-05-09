package com.spendsmart.recurring.client;

import com.spendsmart.recurring.client.fallback.NotificationServiceFallback;
import com.spendsmart.recurring.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/notifications", fallback = NotificationServiceFallback.class)
public interface NotificationServiceClient {

    @PostMapping("/send")
    void sendNotification(@RequestBody NotificationRequest request);
}
