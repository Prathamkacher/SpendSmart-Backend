package com.spendsmart.recurring.dto;

import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.PaymentMethod;
import com.spendsmart.recurring.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RecurringRequest {

    private Long categoryId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private PaymentMethod paymentMethod;
}
