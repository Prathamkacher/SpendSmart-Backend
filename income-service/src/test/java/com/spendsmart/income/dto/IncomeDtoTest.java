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
        IncomeResponse response = new IncomeResponse();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        BigDecimal amount = new BigDecimal("100.00");

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
        
        assertNotNull(response.toString());
        IncomeResponse other = new IncomeResponse();
        other.setIncomeId(1L);
        // Equals check (partial)
        assertNotEquals(response, other);
    }

    @Test
    void testIncomeRequest() {
        IncomeRequest request = new IncomeRequest();
        BigDecimal amount = new BigDecimal("50.00");
        LocalDate today = LocalDate.now();

        request.setCategoryId(1L);
        request.setTitle("Bonus");
        request.setAmount(amount);
        request.setCurrency("EUR");
        request.setSource(IncomeSource.OTHER);
        request.setDate(today);
        request.setNotes("Yearly bonus");
        request.setIsRecurring(false);

        assertEquals(1L, request.getCategoryId());
        assertEquals("Bonus", request.getTitle());
        assertEquals(amount, request.getAmount());
        assertEquals("EUR", request.getCurrency());
        assertEquals(IncomeSource.OTHER, request.getSource());
        assertEquals(today, request.getDate());
        assertEquals("Yearly bonus", request.getNotes());
        assertFalse(request.getIsRecurring());
        
        assertNotNull(request.toString());
    }
}
