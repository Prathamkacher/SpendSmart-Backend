package com.spendsmart.income.dto;

import com.spendsmart.income.entity.IncomeSource;
import com.spendsmart.income.entity.RecurrencePeriod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for creating or updating an income record.
 * Contains validation constraints for income attributes.
 */
@Data
public class IncomeRequest {

    private Long categoryId;

    @NotBlank(message = "Title is required")
    @Size(max = 100)
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3)
    private String currency = "INR";

    @NotNull(message = "Source is required")
    private IncomeSource source;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Size(max = 500)
    private String notes;

    private Boolean isRecurring = false;

    private RecurrencePeriod recurrencePeriod;
}
