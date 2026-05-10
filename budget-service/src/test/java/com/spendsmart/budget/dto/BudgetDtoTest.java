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
        assertEquals(10L, response.getUserId());
        assertEquals(2L, response.getCategoryId());
        assertEquals("Monthly Food", response.getName());
        assertEquals(new BigDecimal("1000.00"), response.getLimitAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals(BudgetPeriod.MONTHLY, response.getPeriod());
        assertEquals(start, response.getStartDate());
        assertEquals(end, response.getEndDate());
        assertEquals(new BigDecimal("200.00"), response.getSpentAmount());
        assertEquals(80, response.getAlertThreshold());
        assertTrue(response.getIsActive());
        assertEquals(20.0, response.getProgressPercentage());
        assertEquals(new BigDecimal("800.00"), response.getRemainingAmount());
        assertEquals("STABLE", response.getStatus());
        
        BudgetResponse response2 = new BudgetResponse(1L, 10L, 2L, "Monthly Food", new BigDecimal("1000.00"), "USD", BudgetPeriod.MONTHLY, start, end, new BigDecimal("200.00"), 80, true, 20.0, new BigDecimal("800.00"), "STABLE");
        assertEquals(response, response2);
        assertEquals(response.hashCode(), response2.hashCode());
        assertNotNull(response.toString());
        
        BudgetResponse response3 = new BudgetResponse();
        response3.setBudgetId(2L);
        assertNotEquals(response, response3);
        
        assertNotNull(BudgetResponse.builder().toString());
    }

    @Test
    void testBudgetRequest() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(1);
        
        BudgetRequest request = BudgetRequest.builder()
            .name("Test")
            .categoryId(1L)
            .limitAmount(new BigDecimal("500.00"))
            .currency("USD")
            .period(BudgetPeriod.MONTHLY)
            .startDate(start)
            .endDate(end)
            .alertThreshold(90)
            .build();

        assertEquals(1L, request.getCategoryId());
        assertEquals("Test", request.getName());
        assertEquals(new BigDecimal("500.00"), request.getLimitAmount());
        assertEquals("USD", request.getCurrency());
        assertEquals(BudgetPeriod.MONTHLY, request.getPeriod());
        assertEquals(start, request.getStartDate());
        assertEquals(end, request.getEndDate());
        assertEquals(90, request.getAlertThreshold());
        
        BudgetRequest request2 = new BudgetRequest("Test", 1L, new BigDecimal("500.00"), "USD", BudgetPeriod.MONTHLY, start, end, 90);
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        BudgetRequest request3 = new BudgetRequest();
        request3.setCategoryId(2L);
        assertNotEquals(request, request3);
        assertNotNull(request.toString());
        
        assertNotNull(BudgetRequest.builder().toString());
    }

    @Test
    void testBudgetUpdateRequest() {
        BudgetUpdateRequest request = BudgetUpdateRequest.builder()
            .userId(1L)
            .categoryId(2L)
            .amount(new BigDecimal("600.00"))
            .build();

        assertEquals(new BigDecimal("600.00"), request.getAmount());
        assertEquals(1L, request.getUserId());
        assertEquals(2L, request.getCategoryId());
        
        BudgetUpdateRequest request2 = new BudgetUpdateRequest(1L, 2L, new BigDecimal("600.00"));
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        BudgetUpdateRequest request3 = new BudgetUpdateRequest();
        request3.setUserId(2L);
        assertNotEquals(request, request3);
        assertNotNull(request.toString());
        
        assertNotNull(BudgetUpdateRequest.builder().toString());
    }
}
