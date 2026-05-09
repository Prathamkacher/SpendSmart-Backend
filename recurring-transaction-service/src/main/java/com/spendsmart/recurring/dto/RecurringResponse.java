package com.spendsmart.recurring.dto;

import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.PaymentMethod;
import com.spendsmart.recurring.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringResponse {

    private Long recurringId;
    private Long userId;
    private Long categoryId;
    private String title;
    private BigDecimal amount;
    private TransactionType type;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextDueDate;
    private Boolean isActive;
    private String description;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
