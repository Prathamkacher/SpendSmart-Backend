package com.spendsmart.analytics.service;

import com.spendsmart.analytics.dto.*;
import java.util.List;
import java.util.Map;

/**
 * Service interface for financial analytics.
 * Defines methods for aggregating transaction data, calculating trends, and generating forecasts.
 */
public interface AnalyticsService {
    void generateMonthlySnapshot(Long userId, int year, int month);
    MonthlySummary getMonthlySummary(Long userId, int year, int month);
    List<MonthlySummary> getYearlySummary(Long userId, int year);
    Map<String, Double> getExpenseBreakdownByCategory(Long userId, int year, int month);
    Map<String, Map<String, Double>> getIncomeVsExpenseTrend(Long userId, int year);
    Map<String, Double> getSavingsRateTrend(Long userId, int year);
    List<TopCategory> getTopSpendingCategories(Long userId, int limit);
    Map<String, Double> getDailyExpenseTrend(Long userId, int year, int month);
    Map<String, Double> getCashflowData(Long userId, int year, int month);
    ForecastResponse getSpendingForecast(Long userId);
    HealthScoreResponse getFinancialHealthScore(Long userId, int year, int month);
}
