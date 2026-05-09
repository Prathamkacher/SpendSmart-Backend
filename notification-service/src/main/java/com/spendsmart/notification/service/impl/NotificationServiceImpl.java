package com.spendsmart.notification.service.impl;

import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.repository.NotificationRepository;
import com.spendsmart.notification.service.NotificationService;
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
    private final JavaMailSender mailSender;
    private final com.spendsmart.notification.client.AuthClient authClient;

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
        
        if (saved.getSeverity() == Notification.Severity.CRITICAL) {
            sendEmail(saved);
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

    private void sendEmail(Notification notification) {
        try {
            com.spendsmart.notification.dto.UserProfileResponse user = authClient.getUserById(notification.getRecipientId());
            if (user == null || user.getEmail() == null) {
                log.warn("Cannot send email: User or email not found for recipientId={}", notification.getRecipientId());
                return;
            }
            String email = user.getEmail();
            log.info("Sending professional HTML critical alert to {}: {}", email, notification.getTitle());
            
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("CRITICAL ALERT: " + notification.getTitle());
            
            String htmlContent = "<!DOCTYPE html><html><head><style>" +
                "body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #fff1f2; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); border-top: 6px solid #e11d48; }" +
                ".header { background-color: #ffffff; padding: 24px; text-align: center; border-bottom: 1px solid #f1f5f9; }" +
                ".header h1 { margin: 0; font-size: 24px; color: #e11d48; font-weight: 800; }" +
                ".content { padding: 40px 32px; color: #334155; line-height: 1.6; font-size: 16px; }" +
                ".alert-title { color: #0f172a; font-size: 20px; font-weight: 700; margin-bottom: 16px; }" +
                ".footer { background-color: #f1f5f9; padding: 24px; text-align: center; color: #64748b; font-size: 13px; }" +
                ".warning-box { background-color: #fff1f2; border: 1px solid #fda4af; padding: 16px; border-radius: 12px; color: #9f1239; font-weight: 500; margin: 24px 0; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h1>SpendSmart Security</h1></div>" +
                "<div class='content'>" +
                "<div class='alert-title'>" + notification.getTitle() + "</div>" +
                "<p>Hello,</p>" +
                "<p>We are notifying you about a critical event regarding your SpendSmart account:</p>" +
                "<div class='warning-box'>" + notification.getMessage() + "</div>" +
                "<p>Please take appropriate action or log in to your dashboard to review the details.</p>" +
                "<br><p>Stay secure,<br><strong>SpendSmart Team</strong></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; " + java.time.Year.now().getValue() + " SpendSmart. All rights reserved.</p>" +
                "</div></div></body></html>";
                
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Critical HTML alert successfully sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send critical HTML alert", e);
        }
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
