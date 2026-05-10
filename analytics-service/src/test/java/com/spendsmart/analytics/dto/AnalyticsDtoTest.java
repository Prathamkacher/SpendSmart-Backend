package com.spendsmart.analytics.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsDtoTest {

    @Test
    void testForecastResponseBuilderAndEquality() {
        ForecastResponse response = buildForecastResponse();
        ForecastResponse same = new ForecastResponse(new BigDecimal("1500.00"), "HIGH", "Looks good");

        assertEquals(new BigDecimal("1500.00"), response.getForecastedExpenses());
        assertEquals("HIGH", response.getConfidence());
        assertEquals("Looks good", response.getMessage());
        assertEquals(response, same);
        assertEquals(response.hashCode(), same.hashCode());
        assertTrue(response.toString().contains("HIGH"));
    }

    @Test
    void testHealthScoreResponseSettersAndAllArgsConstructor() {
        HealthScoreResponse response = new HealthScoreResponse();
        response.setScore(85.0);
        response.setStatus("GOOD");
        response.setInsight("Save more");
        HealthScoreResponse same = new HealthScoreResponse(85.0, "GOOD", "Save more");

        assertEquals(85.0, response.getScore());
        assertEquals("GOOD", response.getStatus());
        assertEquals("Save more", response.getInsight());
        assertEquals(response, same);
        assertEquals(response.hashCode(), same.hashCode());
        assertTrue(response.toString().contains("Save more"));
    }

    @Test
    void testMonthlySummarySettersAndBuilder() {
        MonthlySummary summary = new MonthlySummary();
        summary.setTotalIncome(new BigDecimal("5000"));
        summary.setTotalExpenses(new BigDecimal("3000"));
        summary.setNetSavings(new BigDecimal("2000"));
        summary.setSavingsRate(new BigDecimal("40.0"));
        summary.setTopCategory("Food");
        MonthlySummary same = MonthlySummary.builder()
                .totalIncome(new BigDecimal("5000"))
                .totalExpenses(new BigDecimal("3000"))
                .netSavings(new BigDecimal("2000"))
                .savingsRate(new BigDecimal("40.0"))
                .topCategory("Food")
                .build();

        assertEquals(new BigDecimal("5000"), summary.getTotalIncome());
        assertEquals(new BigDecimal("3000"), summary.getTotalExpenses());
        assertEquals(new BigDecimal("2000"), summary.getNetSavings());
        assertEquals(new BigDecimal("40.0"), summary.getSavingsRate());
        assertEquals("Food", summary.getTopCategory());
        assertEquals(summary, same);
        assertEquals(summary.hashCode(), same.hashCode());
        assertTrue(summary.toString().contains("Food"));
    }

    @Test
    void testTopCategorySettersAndAllArgsConstructor() {
        TopCategory category = new TopCategory();
        category.setCategoryName("Food");
        category.setTotalSpent(new BigDecimal("500"));
        TopCategory same = new TopCategory("Food", new BigDecimal("500"));

        assertEquals("Food", category.getCategoryName());
        assertEquals(new BigDecimal("500"), category.getTotalSpent());
        assertEquals(category, same);
        assertEquals(category.hashCode(), same.hashCode());
        assertTrue(category.toString().contains("Food"));
    }

    private static ForecastResponse buildForecastResponse() {
        return ForecastResponse.builder()
                .forecastedExpenses(new BigDecimal("1500.00"))
                .confidence("HIGH")
                .message("Looks good")
                .build();
    }
}
