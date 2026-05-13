package com.spendsmart.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for budget information within the analytics context.
 * Summarizes budget limits and current spending.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {
    private Long budgetId;
    private Long categoryId;
    private String name;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private Boolean isActive;
}
