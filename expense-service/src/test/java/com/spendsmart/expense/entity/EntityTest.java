package com.spendsmart.expense.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testExpense() {
        Expense expense = new Expense();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        expense.setExpenseId(1L);
        expense.setUserId(2L);
        expense.setCategoryId(3L);
        expense.setTitle("Lunch");
        expense.setAmount(new BigDecimal("15"));
        expense.setCurrency("USD");
        expense.setType(ExpenseType.EXPENSE);
        expense.setPaymentMethod(PaymentMethod.CASH);
        expense.setDate(today);
        expense.setNotes("Notes");
        expense.setReceiptUrl("url");
        expense.setIsRecurring(false);
        expense.setCreatedAt(now);
        expense.setUpdatedAt(now);

        assertEquals(1L, expense.getExpenseId());
        assertEquals("Lunch", expense.getTitle());
        assertEquals(new BigDecimal("15"), expense.getAmount());
        assertFalse(expense.getIsRecurring());
        assertEquals(now, expense.getCreatedAt());

        assertNotNull(expense.toString());
    }
}
