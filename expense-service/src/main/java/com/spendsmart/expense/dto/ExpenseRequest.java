package com.spendsmart.expense.dto;

import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inbound DTO for creating / updating an expense.
 * Jakarta Bean Validation applied on all user-facing fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a valid 3-letter ISO 4217 code")
    private String currency;

    private Long categoryId;

    @NotNull(message = "Expense type is required")
    private ExpenseType type;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Size(max = 500, message = "Receipt URL cannot exceed 500 characters")
    private String receiptUrl;

    private Boolean isRecurring;
}
