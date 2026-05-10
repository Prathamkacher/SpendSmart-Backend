package com.spendsmart.payment.dto;

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
    private String type; // BUDGET_ALERT, RECURRING_DUE, MONTHLY_SUMMARY, SYSTEM, PAYMENT_RECEIPT
    private String severity; // INFO, WARNING, CRITICAL
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType;
}
