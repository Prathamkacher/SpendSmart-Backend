package com.spendsmart.recurring.dto;

import com.spendsmart.recurring.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {
    private Long categoryId;
    private String title;
    private BigDecimal amount;
    private String currency; // "INR" or similar
    private String type; // "EXPENSE"
    private PaymentMethod paymentMethod;
    private LocalDate date;
    private String notes;
    private Boolean isRecurring;
}
