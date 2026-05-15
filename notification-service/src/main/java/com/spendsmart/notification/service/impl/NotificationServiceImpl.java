package com.spendsmart.notification.service.impl;

import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.repository.NotificationRepository;
import com.spendsmart.notification.service.NotificationService;
import com.spendsmart.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
 
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
 
    @Override
    @Transactional
    public NotificationDTO send(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .type(request.getType())
                .severity(request.getSeverity())
                .title(request.getTitle())
                .message(request.getMessage())
                .relatedId(request.getRelatedId())
                .relatedType(request.getRelatedType())
                .isRead(false)
                .isAcknowledged(false)
                .build();
 
        Notification saved = notificationRepository.save(notification);
        
        // Asynchronous calls to EmailService - will not block or rollback if they fail
        if (saved.getSeverity() == Notification.Severity.CRITICAL) {
            emailService.sendCriticalAlertEmail(saved);
        }
        
        if (saved.getType() == Notification.NotificationType.PAYMENT_RECEIPT) {
            emailService.sendReceiptEmail(saved);
        }
 
        return mapToDTO(saved);
    }
 
    @Override
    @Transactional
    public NotificationDTO sendBudgetAlert(Long userId, String message, Double amount) {
        log.info("Creating budget alert for userId={}, amount={}", userId, amount);
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(userId);
        request.setType(Notification.NotificationType.BUDGET_ALERT);
        request.setSeverity(Notification.Severity.CRITICAL);
        request.setTitle("Budget Threshold Exceeded");
        request.setMessage(message + (amount != null ? " (Amount: " + amount + ")" : ""));
        return send(request);
    }
 
    @Override
    @Transactional
    public void sendBulk(List<Long> userIds, String title, String message) {
        List<Notification> notifications = userIds.stream()
                .map(userId -> Notification.builder()
                        .recipientId(userId)
                        .type(Notification.NotificationType.SYSTEM)
                        .severity(Notification.Severity.INFO)
                        .title(title)
                        .message(message)
                        .build())
                .collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
    }
 
    @Override
    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return mapToDTO(notificationRepository.save(notification));
    }
 
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
 
    @Override
    @Transactional
    public NotificationDTO acknowledge(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setAcknowledged(true);
        notification.setRead(true);
        return mapToDTO(notificationRepository.save(notification));
    }
 
    @Override
    public List<NotificationDTO> getByRecipient(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
 
    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsRead(userId, false);
    }
 
    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setDeleted(true);
            notificationRepository.save(notification);
            log.info("Soft-deleted notification: {}", notificationId);
        });
    }
 
    @Override
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
 
    private NotificationDTO mapToDTO(Notification n) {
        return NotificationDTO.builder()
                .notificationId(n.getNotificationId())
                .recipientId(n.getRecipientId())
                .type(n.getType())
                .severity(n.getSeverity())
                .title(n.getTitle())
                .message(n.getMessage())
                .relatedId(n.getRelatedId())
                .relatedType(n.getRelatedType())
                .isRead(n.isRead())
                .isAcknowledged(n.isAcknowledged())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
