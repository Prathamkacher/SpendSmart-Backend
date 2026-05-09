package com.spendsmart.notification.consumer;

import com.spendsmart.notification.config.RabbitMQConfig;
import com.spendsmart.shared.events.NotificationEvent;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.service.NotificationService;
import com.spendsmart.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(NotificationEvent event) {
        log.info("Received notification event from RabbitMQ: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .recipientId(event.getRecipientId())
                    .type(Notification.NotificationType.valueOf(event.getType()))
                    .severity(Notification.Severity.valueOf(event.getSeverity()))
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .relatedId(event.getRelatedId())
                    .relatedType(event.getRelatedType())
                    .build();
            
            notificationService.send(request);
            log.info("Successfully processed and saved notification for recipient: {}", event.getRecipientId());
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }
}
