package com.spendsmart.notification.consumer;

import com.spendsmart.shared.events.NotificationEvent;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationConsumerTest {

    @Test
    void consumeNotification_ShouldMapEventAndSendNotification() {
        NotificationService notificationService = mock(NotificationService.class);
        NotificationConsumer consumer = new NotificationConsumer(notificationService);
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(8L)
                .type("SYSTEM")
                .severity("WARNING")
                .title("Heads up")
                .message("Something happened")
                .relatedId(5L)
                .relatedType("ALERT")
                .build();

        when(notificationService.send(org.mockito.ArgumentMatchers.any(NotificationRequest.class)))
                .thenReturn(NotificationDTO.builder().notificationId(1L).build());

        consumer.consumeNotification(event);

        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).send(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getType()).isEqualTo(Notification.NotificationType.SYSTEM);
        assertThat(requestCaptor.getValue().getSeverity()).isEqualTo(Notification.Severity.WARNING);
        assertThat(requestCaptor.getValue().getRecipientId()).isEqualTo(8L);
    }

    @Test
    void consumeNotification_ShouldSwallowMappingOrServiceErrors() {
        NotificationService notificationService = mock(NotificationService.class);
        NotificationConsumer consumer = new NotificationConsumer(notificationService);
        NotificationEvent invalidEvent = NotificationEvent.builder()
                .recipientId(8L)
                .type("INVALID")
                .severity("INFO")
                .title("Heads up")
                .message("Something happened")
                .build();

        consumer.consumeNotification(invalidEvent);
        verify(notificationService, never()).send(org.mockito.ArgumentMatchers.any(NotificationRequest.class));

        NotificationEvent validEvent = NotificationEvent.builder()
                .recipientId(8L)
                .type("SYSTEM")
                .severity("INFO")
                .title("Heads up")
                .message("Something happened")
                .build();
        doThrow(new IllegalStateException("down"))
                .when(notificationService).send(org.mockito.ArgumentMatchers.any(NotificationRequest.class));

        consumer.consumeNotification(validEvent);

        verify(notificationService).send(org.mockito.ArgumentMatchers.any(NotificationRequest.class));
    }
}
