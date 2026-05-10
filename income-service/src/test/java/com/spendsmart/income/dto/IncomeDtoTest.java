package com.spendsmart.income.dto;

import com.spendsmart.income.entity.IncomeSource;
import com.spendsmart.income.entity.RecurrencePeriod;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class IncomeDtoTest {

    @Test
    void testIncomeResponse() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 11, 30);
        LocalDate today = LocalDate.of(2026, 5, 1);
        BigDecimal amount = new BigDecimal("100.00");
        IncomeResponse response = buildIncomeResponse(now, today, amount);

        assertEquals(1L, response.getIncomeId());
        assertEquals(2L, response.getUserId());
        assertEquals(3L, response.getCategoryId());
        assertEquals("Salary", response.getTitle());
        assertEquals(amount, response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals(IncomeSource.SALARY, response.getSource());
        assertEquals(today, response.getDate());
        assertEquals("Monthly salary", response.getNotes());
        assertTrue(response.getIsRecurring());
        assertEquals(RecurrencePeriod.MONTHLY, response.getRecurrencePeriod());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
        
        IncomeResponse response2 = buildIncomeResponse(now, today, amount);
        
        assertEquals(response, response2);
        assertEquals(response.hashCode(), response2.hashCode());
        assertNotNull(response.toString());
        
        IncomeResponse other = new IncomeResponse();
        other.setIncomeId(2L);
        assertNotEquals(response, other);
    }

    @Test
    void testIncomeRequest() {
        BigDecimal amount = new BigDecimal("50.00");
        LocalDate today = LocalDate.of(2026, 5, 2);
        IncomeRequest request = buildIncomeRequest(today, amount);

        assertEquals(1L, request.getCategoryId());
        assertEquals("Bonus", request.getTitle());
        assertEquals(amount, request.getAmount());
        assertEquals("EUR", request.getCurrency());
        assertEquals(IncomeSource.OTHER, request.getSource());
        assertEquals(today, request.getDate());
        assertEquals("Yearly bonus", request.getNotes());
        assertFalse(request.getIsRecurring());
        assertEquals(RecurrencePeriod.YEARLY, request.getRecurrencePeriod());
        
        IncomeRequest request2 = buildIncomeRequest(today, amount);
        
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        assertNotNull(request.toString());
        
        IncomeRequest other = new IncomeRequest();
        other.setCategoryId(2L);
        assertNotEquals(request, other);
    }

    private IncomeResponse buildIncomeResponse(LocalDateTime now, LocalDate today, BigDecimal amount) {
        IncomeResponse response = new IncomeResponse();
        response.setIncomeId(1L);
        response.setUserId(2L);
        response.setCategoryId(3L);
        response.setTitle("Salary");
        response.setAmount(amount);
        response.setCurrency("USD");
        response.setSource(IncomeSource.SALARY);
        response.setDate(today);
        response.setNotes("Monthly salary");
        response.setIsRecurring(true);
        response.setRecurrencePeriod(RecurrencePeriod.MONTHLY);
        response.setCreatedAt(now);
        response.setUpdatedAt(now);
        return response;
    }

    private IncomeRequest buildIncomeRequest(LocalDate today, BigDecimal amount) {
        IncomeRequest request = new IncomeRequest();
        request.setCategoryId(1L);
        request.setTitle("Bonus");
        request.setAmount(amount);
        request.setCurrency("EUR");
        request.setSource(IncomeSource.OTHER);
        request.setDate(today);
        request.setNotes("Yearly bonus");
        request.setIsRecurring(false);
        request.setRecurrencePeriod(RecurrencePeriod.YEARLY);
        return request;
    }
}
