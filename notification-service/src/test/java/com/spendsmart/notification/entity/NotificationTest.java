package com.spendsmart.notification.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void testNotificationEntity() {
        Notification notification = Notification.builder()
                .notificationId(1L)
                .recipientId(1L)
                .type(Notification.NotificationType.SYSTEM)
                .severity(Notification.Severity.INFO)
                .title("Title")
                .message("Message")
                .isRead(true)
                .isAcknowledged(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(notification.getNotificationId()).isEqualTo(1L);
        assertThat(notification.getRecipientId()).isEqualTo(1L);
        assertThat(notification.getType()).isEqualTo(Notification.NotificationType.SYSTEM);
        assertThat(notification.getSeverity()).isEqualTo(Notification.Severity.INFO);
        assertThat(notification.getTitle()).isEqualTo("Title");
        assertThat(notification.getMessage()).isEqualTo("Message");
        assertThat(notification.isRead()).isTrue();
        assertThat(notification.isAcknowledged()).isTrue();
        assertThat(notification.isDeleted()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();

        notification.setRead(false);
        assertThat(notification.isRead()).isFalse();
    }
}
