package com.spendsmart.budget.dto;

import com.spendsmart.budget.entity.BudgetPeriod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for creating a new budget.
 * Contains constraints and thresholds for spending limits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {

    @NotBlank(message = "Budget name is required")
    private String name;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0.01", message = "Limit amount must be greater than 0")
    private BigDecimal limitAmount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Budget period is required")
    private BudgetPeriod period;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Min(0) @Max(100)
    private Integer alertThreshold;
}
