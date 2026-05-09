package com.spendsmart.analytics.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsDtoTest {

    @Test
    void testForecastResponse() {
        ForecastResponse response = ForecastResponse.builder()
                .forecastedExpenses(new BigDecimal("1500.00"))
                .confidence("HIGH")
                .message("Looks good")
                .build();

        assertEquals(new BigDecimal("1500.00"), response.getForecastedExpenses());
        assertEquals("HIGH", response.getConfidence());
        assertEquals("Looks good", response.getMessage());

        ForecastResponse empty = new ForecastResponse();
        assertNotNull(empty.toString());
    }

    @Test
    void testHealthScoreResponse() {
        HealthScoreResponse response = new HealthScoreResponse();
        response.setScore(85.0);
        response.setStatus("GOOD");
        response.setInsight("Save more");

        assertEquals(85.0, response.getScore());
        assertEquals("GOOD", response.getStatus());
        assertEquals("Save more", response.getInsight());
        assertNotNull(response.toString());
    }

    @Test
    void testMonthlySummary() {
        MonthlySummary summary = new MonthlySummary();
        summary.setTotalIncome(new BigDecimal("5000"));
        summary.setTotalExpenses(new BigDecimal("3000"));
        summary.setNetSavings(new BigDecimal("2000"));

        assertEquals(new BigDecimal("5000"), summary.getTotalIncome());
        assertNotNull(summary.toString());
    }

    @Test
    void testTopCategory() {
        TopCategory category = new TopCategory();
        category.setCategoryName("Food");
        category.setTotalSpent(new BigDecimal("500"));

        assertEquals("Food", category.getCategoryName());
        assertEquals(new BigDecimal("500"), category.getTotalSpent());
        assertNotNull(category.toString());
    }
}
