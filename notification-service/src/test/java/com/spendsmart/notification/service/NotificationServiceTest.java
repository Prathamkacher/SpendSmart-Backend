package com.spendsmart.notification.service;

import com.spendsmart.notification.client.AuthClient;
import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.dto.UserProfileResponse;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.repository.NotificationRepository;
import com.spendsmart.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationRequest request;
    private Notification notification;

    @BeforeEach
    void setUp() {
        request = new NotificationRequest();
        request.setRecipientId(1L);
        request.setType(Notification.NotificationType.BUDGET_ALERT);
        request.setSeverity(Notification.Severity.INFO);
        request.setTitle("Test Title");
        request.setMessage("Test Message");

        notification = Notification.builder()
                .notificationId(1L)
                .recipientId(1L)
                .type(Notification.NotificationType.BUDGET_ALERT)
                .severity(Notification.Severity.INFO)
                .title("Test Title")
                .message("Test Message")
                .isRead(false)
                .build();
    }

    @Test
    void send_ShouldSaveNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationDTO result = notificationService.send(request);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void send_CriticalAlert_ShouldSendEmail() {
        request.setSeverity(Notification.Severity.CRITICAL);
        notification.setSeverity(Notification.Severity.CRITICAL);
        
        UserProfileResponse user = new UserProfileResponse();
        user.setEmail("test@example.com");
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(authClient.getUserById(1L)).thenReturn(user);
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        notificationService.send(request);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void markAsRead_ShouldUpdateStatus() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationDTO result = notificationService.markAsRead(1L);

        assertTrue(result.isRead());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUnreadCount_ShouldReturnCount() {
        when(notificationRepository.countByRecipientIdAndIsRead(1L, false)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5, count);
    }
}
