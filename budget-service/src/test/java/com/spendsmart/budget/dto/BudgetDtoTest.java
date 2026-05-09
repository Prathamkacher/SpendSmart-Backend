package com.spendsmart.budget.dto;

import com.spendsmart.budget.entity.BudgetPeriod;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BudgetDtoTest {

    @Test
    void testBudgetResponse() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(1);
        BudgetResponse response = BudgetResponse.builder()
                .budgetId(1L)
                .userId(10L)
                .categoryId(2L)
                .name("Monthly Food")
                .limitAmount(new BigDecimal("1000.00"))
                .currency("USD")
                .period(BudgetPeriod.MONTHLY)
                .startDate(start)
                .endDate(end)
                .spentAmount(new BigDecimal("200.00"))
                .alertThreshold(80)
                .isActive(true)
                .progressPercentage(20.0)
                .remainingAmount(new BigDecimal("800.00"))
                .status("STABLE")
                .build();

        assertEquals(1L, response.getBudgetId());
        assertEquals("Monthly Food", response.getName());
        assertEquals(new BigDecimal("1000.00"), response.getLimitAmount());
        assertEquals(BudgetPeriod.MONTHLY, response.getPeriod());
        assertEquals(start, response.getStartDate());
        assertEquals(end, response.getEndDate());
        assertEquals(new BigDecimal("200.00"), response.getSpentAmount());
        assertEquals(80, response.getAlertThreshold());
        assertTrue(response.getIsActive());
        assertEquals(20.0, response.getProgressPercentage());
        assertEquals(new BigDecimal("800.00"), response.getRemainingAmount());
        assertEquals("STABLE", response.getStatus());

        BudgetResponse empty = new BudgetResponse();
        assertNotNull(empty.toString());
    }

    @Test
    void testBudgetRequest() {
        BudgetRequest request = new BudgetRequest();
        request.setCategoryId(1L);
        request.setLimitAmount(new BigDecimal("500.00"));
        request.setPeriod(BudgetPeriod.MONTHLY);
        request.setAlertThreshold(90);

        assertEquals(1L, request.getCategoryId());
        assertEquals(new BigDecimal("500.00"), request.getLimitAmount());
        assertEquals(BudgetPeriod.MONTHLY, request.getPeriod());
        assertEquals(90, request.getAlertThreshold());
        
        assertNotNull(request.toString());
    }

    @Test
    void testBudgetUpdateRequest() {
        BudgetUpdateRequest request = new BudgetUpdateRequest();
        request.setAmount(new BigDecimal("600.00"));
        request.setUserId(1L);
        request.setCategoryId(2L);

        assertEquals(new BigDecimal("600.00"), request.getAmount());
        assertEquals(1L, request.getUserId());
        
        assertNotNull(request.toString());
    }
}
