package com.spendsmart.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for updating an existing budget's spending amount.
 * Typically used to increment or set the 'spent' total.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUpdateRequest {
    private Long userId;
    private Long categoryId;
    private BigDecimal amount;
}
