package com.spendsmart.notification.dto;

import com.spendsmart.notification.entity.Notification;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationDtoTest {

    @Test
    void testNotificationDTOBuilderAndAccessors() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 10, 15);
        NotificationDTO dto = buildNotificationDTO(now);

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
    }

    @Test
    void testNotificationDTOEqualitySettersAndAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 10, 15);
        NotificationDTO dto = buildNotificationDTO(now);
        NotificationDTO same = new NotificationDTO(
                1L,
                2L,
                Notification.NotificationType.SYSTEM,
                Notification.Severity.INFO,
                "New Expense",
                "You added a new expense",
                100L,
                "EXPENSE",
                false,
                false,
                now
        );
        NotificationDTO mutable = new NotificationDTO();
        mutable.setNotificationId(1L);
        mutable.setRecipientId(2L);
        mutable.setType(Notification.NotificationType.SYSTEM);
        mutable.setSeverity(Notification.Severity.INFO);
        mutable.setTitle("New Expense");
        mutable.setMessage("You added a new expense");
        mutable.setRelatedId(100L);
        mutable.setRelatedType("EXPENSE");
        mutable.setRead(false);
        mutable.setAcknowledged(false);
        mutable.setCreatedAt(now);

        assertEquals(dto, same);
        assertEquals(dto, mutable);
        assertEquals(dto.hashCode(), same.hashCode());
        assertTrue(dto.toString().contains("New Expense"));
    }

    @Test
    void testNotificationRequestBuilderAndAccessors() {
        NotificationRequest request = buildNotificationRequest();

        assertEquals(1L, request.getRecipientId());
        assertEquals(Notification.NotificationType.SYSTEM, request.getType());
        assertEquals(Notification.Severity.CRITICAL, request.getSeverity());
        assertEquals("Alert", request.getTitle());
        assertEquals("System maintenance", request.getMessage());
        assertEquals(77L, request.getRelatedId());
        assertEquals("SYSTEM", request.getRelatedType());
    }

    @Test
    void testNotificationRequestEqualityAndUserProfileResponse() {
        NotificationRequest request = buildNotificationRequest();
        NotificationRequest same = new NotificationRequest(
                1L,
                Notification.NotificationType.SYSTEM,
                Notification.Severity.CRITICAL,
                "Alert",
                "System maintenance",
                77L,
                "SYSTEM"
        );
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId(9L)
                .email("alex@example.com")
                .fullName("Alex Spend")
                .currency("INR")
                .build();
        UserProfileResponse profileCopy = new UserProfileResponse(9L, "alex@example.com", "Alex Spend", "INR");

        assertEquals(request, same);
        assertEquals(request.hashCode(), same.hashCode());
        assertTrue(request.toString().contains("Alert"));

        assertEquals(profile, profileCopy);
        assertEquals(profile.hashCode(), profileCopy.hashCode());
        assertEquals("Alex Spend", profile.getFullName());
        assertTrue(profile.toString().contains("alex@example.com"));
    }

    private static NotificationDTO buildNotificationDTO(LocalDateTime now) {
        return NotificationDTO.builder()
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
    }

    private static NotificationRequest buildNotificationRequest() {
        return NotificationRequest.builder()
                .recipientId(1L)
                .type(Notification.NotificationType.SYSTEM)
                .severity(Notification.Severity.CRITICAL)
                .title("Alert")
                .message("System maintenance")
                .relatedId(77L)
                .relatedType("SYSTEM")
                .build();
    }
}
