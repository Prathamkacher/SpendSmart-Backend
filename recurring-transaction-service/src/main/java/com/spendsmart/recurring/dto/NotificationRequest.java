package com.spendsmart.recurring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long recipientId;
    private String type; // RECURRING_DUE
    private String severity; // INFO
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType;
}
