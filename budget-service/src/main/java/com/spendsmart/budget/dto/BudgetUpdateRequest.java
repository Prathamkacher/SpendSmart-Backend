package com.spendsmart.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUpdateRequest {
    private Long userId;
    private Long categoryId;
    private BigDecimal amount;
}
