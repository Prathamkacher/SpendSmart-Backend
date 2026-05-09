package com.spendsmart.budget.dto;

import com.spendsmart.budget.entity.BudgetPeriod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long budgetId;
    private Long userId;
    private Long categoryId;
    private String name;
    private BigDecimal limitAmount;
    private String currency;
    private BudgetPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal spentAmount;
    private Integer alertThreshold;
    private Boolean isActive;
    
    // Calculated fields
    private Double progressPercentage;
    private BigDecimal remainingAmount;
    private String status; // STABLE, WARNING, EXCEEDED
}
