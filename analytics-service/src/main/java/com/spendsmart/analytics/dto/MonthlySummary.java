package com.spendsmart.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private BigDecimal savingsRate;
    private String topCategory;
}
