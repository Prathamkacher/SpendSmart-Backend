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
public class ForecastResponse {
    private BigDecimal forecastedExpenses;
    private String confidence; // e.g., "HIGH", "MEDIUM", "LOW"
    private String message;
}
