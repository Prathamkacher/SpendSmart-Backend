package com.spendsmart.notification.service;

import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.dto.NotificationRequest;
import java.util.List;

public interface NotificationService {
    NotificationDTO send(NotificationRequest request);
    NotificationDTO sendBudgetAlert(Long userId, String message, Double amount);
    void sendBulk(List<Long> userIds, String title, String message);
    NotificationDTO markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    NotificationDTO acknowledge(Long notificationId);
    List<NotificationDTO> getByRecipient(Long userId);
    long getUnreadCount(Long userId);
    void deleteNotification(Long notificationId);
    List<NotificationDTO> getAllNotifications();
}
