package com.spendsmart.expense.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO sent to Budget-Service via Feign to update spent amounts.
 * Positive amount = increment, negative = decrement.
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
