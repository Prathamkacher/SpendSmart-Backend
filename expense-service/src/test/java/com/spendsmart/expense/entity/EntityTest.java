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
        
        Expense expense2 = new Expense(1L, 2L, 3L, "Lunch", new BigDecimal("15"), "USD", ExpenseType.EXPENSE, PaymentMethod.CASH, today, "Notes", "url", false, now, now);
        assertEquals(expense, expense2);
        assertEquals(expense.hashCode(), expense2.hashCode());
        
        Expense expense3 = new Expense();
        expense3.setExpenseId(2L);
        assertNotEquals(expense, expense3);
        
        Expense expense4 = Expense.builder()
            .expenseId(1L)
            .userId(2L)
            .categoryId(3L)
            .title("Lunch")
            .amount(new BigDecimal("15"))
            .currency("USD")
            .type(ExpenseType.EXPENSE)
            .paymentMethod(PaymentMethod.CASH)
            .date(today)
            .notes("Notes")
            .receiptUrl("url")
            .isRecurring(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
            
        assertEquals(expense, expense4);
        assertNotNull(expense.toString());
        assertNotNull(Expense.builder().toString());
    }

    @Test
    void testEnums() {
        ExpenseType[] typeValues = ExpenseType.values();
        assertTrue(typeValues.length > 0);
        assertEquals(ExpenseType.EXPENSE, ExpenseType.valueOf("EXPENSE"));
        
        PaymentMethod[] methodValues = PaymentMethod.values();
        assertTrue(methodValues.length > 0);
        assertEquals(PaymentMethod.CASH, PaymentMethod.valueOf("CASH"));
    }
}
