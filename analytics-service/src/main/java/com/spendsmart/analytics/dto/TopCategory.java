package com.spendsmart.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Data Transfer Object for high-spending categories.
 * Represents a category and the total amount spent within it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCategory {
    private String categoryName;
    private BigDecimal totalSpent;
}
