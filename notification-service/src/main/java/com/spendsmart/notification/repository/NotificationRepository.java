package com.spendsmart.notification.repository;

import com.spendsmart.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long recipientId, boolean isRead);
    
    long countByRecipientIdAndIsRead(Long recipientId, boolean isRead);
    
    List<Notification> findByType(Notification.NotificationType type);
    
    List<Notification> findBySeverity(Notification.Severity severity);
    
    List<Notification> findByIsAcknowledged(boolean isAcknowledged);
    
    void deleteByNotificationId(Long notificationId);
}
