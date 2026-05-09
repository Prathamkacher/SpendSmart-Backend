package com.spendsmart.expense.dto;

import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.entity.PaymentMethod;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ExpenseDtoTest {

    @Test
    void testExpenseResponse() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        ExpenseResponse response = ExpenseResponse.builder()
                .expenseId(1L)
                .userId(100L)
                .categoryId(5L)
                .title("Groceries")
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .type(ExpenseType.EXPENSE)
                .paymentMethod(PaymentMethod.CASH)
                .date(today)
                .notes("Weekly groceries")
                .receiptUrl("http://receipt.url")
                .isRecurring(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(1L, response.getExpenseId());
        assertEquals(100L, response.getUserId());
        assertEquals(5L, response.getCategoryId());
        assertEquals("Groceries", response.getTitle());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals(ExpenseType.EXPENSE, response.getType());
        assertEquals(PaymentMethod.CASH, response.getPaymentMethod());
        assertEquals(today, response.getDate());
        assertEquals("Weekly groceries", response.getNotes());
        assertEquals("http://receipt.url", response.getReceiptUrl());
        assertFalse(response.getIsRecurring());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());

        ExpenseResponse empty = new ExpenseResponse();
        assertNotNull(empty.toString());
    }

    @Test
    void testExpenseRequest() {
        ExpenseRequest request = new ExpenseRequest();
        request.setCategoryId(2L);
        request.setTitle("Rent");
        request.setAmount(new BigDecimal("1200.00"));
        request.setCurrency("USD");
        request.setType(ExpenseType.EXPENSE);
        request.setPaymentMethod(PaymentMethod.BANK);
        request.setDate(LocalDate.now());
        request.setNotes("Monthly rent");
        request.setIsRecurring(true);

        assertEquals(2L, request.getCategoryId());
        assertEquals("Rent", request.getTitle());
        assertEquals(new BigDecimal("1200.00"), request.getAmount());
        assertTrue(request.getIsRecurring());
        
        assertNotNull(request.toString());
    }
}
