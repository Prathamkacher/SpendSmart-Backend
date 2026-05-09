package com.spendsmart.notification.dto;

import com.spendsmart.notification.entity.Notification;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class NotificationDtoTest {

    @Test
    void testNotificationDTO() {
        LocalDateTime now = LocalDateTime.now();
        NotificationDTO dto = NotificationDTO.builder()
                .notificationId(1L)
                .recipientId(2L)
                .type(Notification.NotificationType.SYSTEM)
                .severity(Notification.Severity.INFO)
                .title("New Expense")
                .message("You added a new expense")
                .relatedId(100L)
                .relatedType("EXPENSE")
                .isRead(false)
                .isAcknowledged(false)
                .createdAt(now)
                .build();

        assertEquals(1L, dto.getNotificationId());
        assertEquals(2L, dto.getRecipientId());
        assertEquals(Notification.NotificationType.SYSTEM, dto.getType());
        assertEquals(Notification.Severity.INFO, dto.getSeverity());
        assertEquals("New Expense", dto.getTitle());
        assertEquals("You added a new expense", dto.getMessage());
        assertEquals(100L, dto.getRelatedId());
        assertEquals("EXPENSE", dto.getRelatedType());
        assertFalse(dto.isRead());
        assertFalse(dto.isAcknowledged());
        assertEquals(now, dto.getCreatedAt());

        NotificationDTO empty = new NotificationDTO();
        empty.setRead(true);
        assertTrue(empty.isRead());
        assertNotNull(empty.toString());
    }

    @Test
    void testNotificationRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(1L);
        request.setType(Notification.NotificationType.SYSTEM);
        request.setSeverity(Notification.Severity.CRITICAL);
        request.setTitle("Alert");
        request.setMessage("System maintenance");

        assertEquals(1L, request.getRecipientId());
        assertEquals(Notification.NotificationType.SYSTEM, request.getType());
        assertEquals(Notification.Severity.CRITICAL, request.getSeverity());
        
        assertNotNull(request.toString());
    }
}
