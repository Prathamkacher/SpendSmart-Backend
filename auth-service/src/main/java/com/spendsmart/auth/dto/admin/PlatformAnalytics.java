package com.spendsmart.auth.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object for high-level platform analytics.
 * Summarizes user activity, transaction volume, and financial trends.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformAnalytics {
    private long totalUsers;
    private long totalTransactions;
    private BigDecimal totalExpenses;
    private BigDecimal totalIncome;
    private BigDecimal avgSpendingPerUser;
    private Map<String, Long> userRegistrationTrend; // Month -> Count
}
