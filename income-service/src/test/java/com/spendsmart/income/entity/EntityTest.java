package com.spendsmart.income.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testIncome() {
        Income income = new Income();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        income.setIncomeId(1L);
        income.setUserId(2L);
        income.setCategoryId(3L);
        income.setTitle("Salary");
        income.setAmount(new BigDecimal("5000"));
        income.setCurrency("USD");
        income.setSource(IncomeSource.SALARY);
        income.setDate(today);
        income.setNotes("Notes");
        income.setIsRecurring(true);
        income.setRecurrencePeriod(RecurrencePeriod.MONTHLY);
        income.setCreatedAt(now);
        income.setUpdatedAt(now);

        assertEquals(1L, income.getIncomeId());
        assertEquals("Salary", income.getTitle());
        assertEquals(new BigDecimal("5000"), income.getAmount());
        assertTrue(income.getIsRecurring());
        assertEquals(now, income.getCreatedAt());

        assertNotNull(income.toString());
    }
}
