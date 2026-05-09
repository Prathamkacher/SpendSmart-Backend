package com.spendsmart.notification.controller;

import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationDTO> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.send(request));
    }

    @PostMapping("/send-budget-alert")
    public ResponseEntity<NotificationDTO> sendBudgetAlert(
            @RequestParam Long userId,
            @RequestParam String message,
            @RequestParam Double amount) {
        return ResponseEntity.ok(notificationService.sendBudgetAlert(userId, message, amount));
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<Void> sendBulk(@RequestBody BulkNotificationRequest request) {
        notificationService.sendBulk(request.getUserIds(), request.getTitle(), request.getMessage());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDTO>> getByRecipient(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getByRecipient(userId));
    }

    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/acknowledge/{id}")
    public ResponseEntity<NotificationDTO> acknowledge(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.acknowledge(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationDTO>> getAll() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    // Helper DTO for bulk requests
    public static class BulkNotificationRequest {
        private List<Long> userIds;
        private String title;
        private String message;
        
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
