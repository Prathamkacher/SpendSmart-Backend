package com.spendsmart.recurring.client.fallback;

import com.spendsmart.recurring.client.NotificationServiceClient;
import com.spendsmart.recurring.dto.NotificationRequest;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationServiceFallback implements NotificationServiceClient {

    @Override
    public void sendNotification(NotificationRequest request) {
        log.error("Failed to send notification to recipientId {}. Notification Service is down.", request.getRecipientId());
    }
}
