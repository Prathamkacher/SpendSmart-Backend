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
        budget.setSpentAmount(new BigDecimal("100"));
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
        
        Budget budget2 = new Budget(1L, 2L, 3L, "Test Budget", new BigDecimal("1000"), "USD", BudgetPeriod.MONTHLY, today, today.plusMonths(1), new BigDecimal("100"), 80, true, now, now);
        assertEquals(budget, budget2);
        assertEquals(budget.hashCode(), budget2.hashCode());
        
        Budget budget3 = new Budget();
        budget3.setBudgetId(2L);
        assertNotEquals(budget, budget3);
        
        Budget budget4 = Budget.builder()
            .budgetId(1L)
            .userId(2L)
            .categoryId(3L)
            .name("Test Budget")
            .limitAmount(new BigDecimal("1000"))
            .currency("USD")
            .period(BudgetPeriod.MONTHLY)
            .startDate(today)
            .endDate(today.plusMonths(1))
            .spentAmount(new BigDecimal("100"))
            .alertThreshold(80)
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .build();
            
        assertEquals(budget, budget4);
        assertNotNull(Budget.builder().toString());
    }

    @Test
    void testBudgetPeriodEnum() {
        BudgetPeriod[] values = BudgetPeriod.values();
        assertTrue(values.length > 0);
        
        BudgetPeriod period = BudgetPeriod.valueOf("MONTHLY");
        assertEquals(BudgetPeriod.MONTHLY, period);
    }
}
