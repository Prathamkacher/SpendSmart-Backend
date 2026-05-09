package com.spendsmart.expense.dto;

import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.entity.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outbound DTO returned by all expense query endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long expenseId;
    private Long userId;
    private Long categoryId;
    private String title;
    private BigDecimal amount;
    private String currency;
    private ExpenseType type;
    private PaymentMethod paymentMethod;
    private LocalDate date;
    private String notes;
    private String receiptUrl;
    private Boolean isRecurring;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
