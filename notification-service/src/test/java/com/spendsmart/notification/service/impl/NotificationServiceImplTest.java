package com.spendsmart.notification.service.impl;

import com.spendsmart.notification.client.AuthClient;
import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.dto.UserProfileResponse;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Unit Tests")
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private AuthClient authClient;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;
    private NotificationRequest notificationRequest;
    private final Long USER_ID = 1L;
    private final Long NOTIFICATION_ID = 1L;

    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
                .notificationId(NOTIFICATION_ID)
                .recipientId(USER_ID)
                .type(Notification.NotificationType.SYSTEM)
                .severity(Notification.Severity.INFO)
                .title("Test Notification")
                .message("Test Message")
                .build();

        notificationRequest = new NotificationRequest();
        notificationRequest.setRecipientId(USER_ID);
        notificationRequest.setType(Notification.NotificationType.SYSTEM);
        notificationRequest.setSeverity(Notification.Severity.INFO);
        notificationRequest.setTitle("Test Notification");
        notificationRequest.setMessage("Test Message");
    }

    @Test
    @DisplayName("send() - should save notification")
    void send_ShouldSaveNotification() {
        when(notificationRepository.save(any())).thenReturn(testNotification);

        NotificationDTO result = notificationService.send(notificationRequest);

        assertThat(result).isNotNull();
        verify(notificationRepository).save(any());
    }

    @Test
    @DisplayName("send() - should send email for CRITICAL severity")
    void send_Critical_ShouldSendEmail() {
        testNotification.setSeverity(Notification.Severity.CRITICAL);
        notificationRequest.setSeverity(Notification.Severity.CRITICAL);
        
        UserProfileResponse userProfile = new UserProfileResponse();
        userProfile.setEmail("test@example.com");

        when(notificationRepository.save(any())).thenReturn(testNotification);
        when(authClient.getUserById(USER_ID)).thenReturn(userProfile);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        notificationService.send(notificationRequest);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendBudgetAlert() - should delegate to send with correct parameters")
    void sendBudgetAlert_ShouldDelegate() {
        when(notificationRepository.save(any())).thenReturn(testNotification);

        NotificationDTO result = notificationService.sendBudgetAlert(USER_ID, "Budget hit", 500.0);

        assertThat(result).isNotNull();
        verify(notificationRepository).save(argThat(n -> 
            n.getType() == Notification.NotificationType.BUDGET_ALERT &&
            n.getMessage().contains("500.0")));
    }

    @Test
    @DisplayName("sendBulk() - should save multiple notifications")
    void sendBulk_ShouldSaveAll() {
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        notificationService.sendBulk(userIds, "System Update", "Down for maintenance");

        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("sendEmail() - should handle auth service failure gracefully")
    void send_Critical_AuthFailure_ShouldNotThrowException() {
        testNotification.setSeverity(Notification.Severity.CRITICAL);
        notificationRequest.setSeverity(Notification.Severity.CRITICAL);
        
        when(notificationRepository.save(any())).thenReturn(testNotification);
        when(authClient.getUserById(USER_ID)).thenThrow(new RuntimeException("Auth service down"));

        // Should not throw exception, just log and continue
        NotificationDTO result = notificationService.send(notificationRequest);
        
        assertThat(result).isNotNull();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("acknowledge() - should mark as read and acknowledged")
    void acknowledge_ShouldUpdateBothFlags() {
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any())).thenReturn(testNotification);

        notificationService.acknowledge(NOTIFICATION_ID);

        assertThat(testNotification.isRead()).isTrue();
        assertThat(testNotification.isAcknowledged()).isTrue();
    }

    @Test
    @DisplayName("markAsRead() - should mark notification as read")
    void markAsRead_ShouldUpdateFlag() {
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any())).thenReturn(testNotification);

        notificationService.markAsRead(NOTIFICATION_ID);

        assertThat(testNotification.isRead()).isTrue();
    }

    @Test
    @DisplayName("markAllAsRead() - should mark all user notifications as read")
    void markAllAsRead_ShouldUpdateAll() {
        List<Notification> unread = Arrays.asList(testNotification, new Notification());
        when(notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(USER_ID, false)).thenReturn(unread);

        notificationService.markAllAsRead(USER_ID);

        verify(notificationRepository).saveAll(unread);
        assertThat(testNotification.isRead()).isTrue();
    }

    @Test
    @DisplayName("getByRecipient() - should return list of DTOs")
    void getByRecipient_ShouldReturnList() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(USER_ID)).thenReturn(Collections.singletonList(testNotification));

        List<NotificationDTO> results = notificationService.getByRecipient(USER_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getNotificationId()).isEqualTo(NOTIFICATION_ID);
    }

    @Test
    @DisplayName("getUnreadCount() - should return count")
    void getUnreadCount_ShouldReturnCount() {
        when(notificationRepository.countByRecipientIdAndIsRead(USER_ID, false)).thenReturn(5L);

        long count = notificationService.getUnreadCount(USER_ID);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("deleteNotification() - should soft delete")
    void deleteNotification_ShouldSoftDelete() {
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));

        notificationService.deleteNotification(NOTIFICATION_ID);

        verify(notificationRepository).save(argThat(Notification::isDeleted));
    }

    @Test
    @DisplayName("getAllNotifications() - should return all")
    void getAllNotifications_ShouldReturnAll() {
        when(notificationRepository.findAll()).thenReturn(Collections.singletonList(testNotification));

        List<NotificationDTO> results = notificationService.getAllNotifications();

        assertThat(results).hasSize(1);
    }
    @Test
    @DisplayName("markAsRead() - should throw exception if not found")
    void markAsRead_NotFound_ShouldThrowException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("acknowledge() - should throw exception if not found")
    void acknowledge_NotFound_ShouldThrowException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.acknowledge(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("sendEmail() - should handle null user email profile")
    void sendEmail_NullEmail_ShouldLogWarning() {
        testNotification.setSeverity(Notification.Severity.CRITICAL);
        notificationRequest.setSeverity(Notification.Severity.CRITICAL);
        
        UserProfileResponse userProfile = new UserProfileResponse();
        userProfile.setEmail(null);

        when(notificationRepository.save(any())).thenReturn(testNotification);
        when(authClient.getUserById(USER_ID)).thenReturn(userProfile);

        notificationService.send(notificationRequest);

        verify(mailSender, never()).createMimeMessage();
    }
}
