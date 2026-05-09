package com.spendsmart.notification.dto;

import com.spendsmart.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotNull(message = "Type is required")
    private Notification.NotificationType type;

    @NotNull(message = "Severity is required")
    private Notification.Severity severity;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private Long relatedId;
    private String relatedType;
}
