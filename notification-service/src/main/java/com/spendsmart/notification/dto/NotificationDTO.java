package com.spendsmart.notification.dto;

import com.spendsmart.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long notificationId;
    private Long recipientId;
    private Notification.NotificationType type;
    private Notification.Severity severity;
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType;
    @JsonProperty("isRead")
    private boolean isRead;

    @JsonProperty("isAcknowledged")
    private boolean isAcknowledged;
    private LocalDateTime createdAt;
}
