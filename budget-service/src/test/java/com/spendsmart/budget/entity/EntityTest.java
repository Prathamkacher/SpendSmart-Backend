package com.spendsmart.budget.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testBudget() {
        Budget budget = new Budget();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        budget.setBudgetId(1L);
        budget.setUserId(2L);
        budget.setCategoryId(3L);
        budget.setName("Test Budget");
        budget.setLimitAmount(new BigDecimal("1000"));
        budget.setCurrency("USD");
        budget.setPeriod(BudgetPeriod.MONTHLY);
        budget.setStartDate(today);
        budget.setEndDate(today.plusMonths(1));
        budget.setAlertThreshold(80);
        budget.setIsActive(true);
        budget.setCreatedAt(now);
        budget.setUpdatedAt(now);

        assertEquals(1L, budget.getBudgetId());
        assertEquals("Test Budget", budget.getName());
        assertEquals(new BigDecimal("1000"), budget.getLimitAmount());
        assertEquals(BudgetPeriod.MONTHLY, budget.getPeriod());
        assertTrue(budget.getIsActive());
        assertEquals(now, budget.getCreatedAt());

        assertNotNull(budget.toString());
    }
}
