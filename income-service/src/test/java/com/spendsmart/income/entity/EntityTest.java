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
        assertEquals(2L, income.getUserId());
        assertEquals(3L, income.getCategoryId());
        assertEquals("Salary", income.getTitle());
        assertEquals(new BigDecimal("5000"), income.getAmount());
        assertEquals("USD", income.getCurrency());
        assertEquals(IncomeSource.SALARY, income.getSource());
        assertEquals(today, income.getDate());
        assertEquals("Notes", income.getNotes());
        assertTrue(income.getIsRecurring());
        assertEquals(RecurrencePeriod.MONTHLY, income.getRecurrencePeriod());
        assertEquals(now, income.getCreatedAt());
        assertEquals(now, income.getUpdatedAt());

        assertNotNull(income.toString());
        
        Income income2 = new Income(1L, 2L, 3L, "Salary", new BigDecimal("5000"), "USD", IncomeSource.SALARY, today, "Notes", true, RecurrencePeriod.MONTHLY, now, now);
        assertEquals(income, income2);
        assertEquals(income.hashCode(), income2.hashCode());
        
        Income income3 = new Income();
        income3.setIncomeId(2L);
        assertNotEquals(income, income3);
        
        Income income4 = Income.builder()
            .incomeId(1L)
            .userId(2L)
            .categoryId(3L)
            .title("Salary")
            .amount(new BigDecimal("5000"))
            .currency("USD")
            .source(IncomeSource.SALARY)
            .date(today)
            .notes("Notes")
            .isRecurring(true)
            .recurrencePeriod(RecurrencePeriod.MONTHLY)
            .createdAt(now)
            .updatedAt(now)
            .build();
            
        assertEquals(income, income4);
        assertNotNull(Income.builder().toString());
    }

    @Test
    void testEnums() {
        IncomeSource[] sourceValues = IncomeSource.values();
        assertTrue(sourceValues.length > 0);
        assertEquals(IncomeSource.SALARY, IncomeSource.valueOf("SALARY"));
        
        RecurrencePeriod[] periodValues = RecurrencePeriod.values();
        assertTrue(periodValues.length > 0);
        assertEquals(RecurrencePeriod.MONTHLY, RecurrencePeriod.valueOf("MONTHLY"));
    }
}
