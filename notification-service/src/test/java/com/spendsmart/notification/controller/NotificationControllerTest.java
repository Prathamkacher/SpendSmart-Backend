package com.spendsmart.notification.controller;

import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Mock private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationDTO notificationDTO;

    @BeforeEach
    void setUp() {
        notificationDTO = NotificationDTO.builder()
                .notificationId(1L)
                .title("Test")
                .build();
    }

    @Test
    @DisplayName("send() - should return OK")
    void send_ShouldReturnOk() {
        NotificationRequest request = new NotificationRequest();
        when(notificationService.send(any())).thenReturn(notificationDTO);

        ResponseEntity<NotificationDTO> response = notificationController.send(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("Test");
    }

    @Test
    @DisplayName("sendBudgetAlert() - should return OK")
    void sendBudgetAlert_ShouldReturnOk() {
        when(notificationService.sendBudgetAlert(eq(1L), any(), any())).thenReturn(notificationDTO);

        ResponseEntity<NotificationDTO> response = notificationController.sendBudgetAlert(1L, "alert", 500.0);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("sendBulk() - should return OK")
    void sendBulk_ShouldReturnOk() {
        NotificationController.BulkNotificationRequest req = new NotificationController.BulkNotificationRequest();
        req.setUserIds(List.of(1L, 2L));
        req.setTitle("Title");
        req.setMessage("Msg");

        ResponseEntity<Void> response = notificationController.sendBulk(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).sendBulk(List.of(1L, 2L), "Title", "Msg");
    }

    @Test
    @DisplayName("getByRecipient() - should return list")
    void getByRecipient_ShouldReturnList() {
        when(notificationService.getByRecipient(1L)).thenReturn(Collections.singletonList(notificationDTO));

        ResponseEntity<List<NotificationDTO>> response = notificationController.getByRecipient(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getUnreadCount() - should return count")
    void getUnreadCount_ShouldReturnCount() {
        when(notificationService.getUnreadCount(1L)).thenReturn(5L);

        ResponseEntity<Long> response = notificationController.getUnreadCount(1L);

        assertThat(response.getBody()).isEqualTo(5L);
    }

    @Test
    @DisplayName("markAsRead() - should return OK")
    void markAsRead_ShouldReturnOk() {
        when(notificationService.markAsRead(1L)).thenReturn(notificationDTO);

        ResponseEntity<NotificationDTO> response = notificationController.markAsRead(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("markAllAsRead() - should return OK")
    void markAllAsRead_ShouldReturnOk() {
        ResponseEntity<Void> response = notificationController.markAllAsRead(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).markAllAsRead(1L);
    }

    @Test
    @DisplayName("acknowledge() - should return OK")
    void acknowledge_ShouldReturnOk() {
        when(notificationService.acknowledge(1L)).thenReturn(notificationDTO);

        ResponseEntity<NotificationDTO> response = notificationController.acknowledge(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteNotification() - should return OK")
    void deleteNotification_ShouldReturnOk() {
        ResponseEntity<Void> response = notificationController.deleteNotification(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).deleteNotification(1L);
    }

    @Test
    @DisplayName("getAll() - should return OK")
    void getAll_ShouldReturnOk() {
        when(notificationService.getAllNotifications()).thenReturn(Collections.singletonList(notificationDTO));

        ResponseEntity<List<NotificationDTO>> response = notificationController.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
