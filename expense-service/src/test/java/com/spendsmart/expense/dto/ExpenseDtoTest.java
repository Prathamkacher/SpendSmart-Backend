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
        
        ExpenseResponse response2 = new ExpenseResponse(1L, 100L, 5L, "Groceries", new BigDecimal("50.00"), "USD", ExpenseType.EXPENSE, PaymentMethod.CASH, today, "Weekly groceries", "http://receipt.url", false, now, now);
        assertEquals(response, response2);
        assertEquals(response.hashCode(), response2.hashCode());
        
        ExpenseResponse response3 = new ExpenseResponse();
        response3.setExpenseId(2L);
        assertNotEquals(response, response3);
        
        assertNotNull(response.toString());
        assertNotNull(ExpenseResponse.builder().toString());
    }

    @Test
    void testExpenseRequest() {
        LocalDate today = LocalDate.now();
        ExpenseRequest request = ExpenseRequest.builder()
            .title("Rent")
            .amount(new BigDecimal("1200.00"))
            .currency("USD")
            .categoryId(2L)
            .type(ExpenseType.EXPENSE)
            .paymentMethod(PaymentMethod.BANK)
            .date(today)
            .notes("Monthly rent")
            .receiptUrl("url")
            .isRecurring(true)
            .build();

        assertEquals(2L, request.getCategoryId());
        assertEquals("Rent", request.getTitle());
        assertEquals(new BigDecimal("1200.00"), request.getAmount());
        assertEquals("USD", request.getCurrency());
        assertEquals(ExpenseType.EXPENSE, request.getType());
        assertEquals(PaymentMethod.BANK, request.getPaymentMethod());
        assertEquals(today, request.getDate());
        assertEquals("Monthly rent", request.getNotes());
        assertEquals("url", request.getReceiptUrl());
        assertTrue(request.getIsRecurring());
        
        ExpenseRequest request2 = new ExpenseRequest("Rent", new BigDecimal("1200.00"), "USD", 2L, ExpenseType.EXPENSE, PaymentMethod.BANK, today, "Monthly rent", "url", true);
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        ExpenseRequest request3 = new ExpenseRequest();
        request3.setCategoryId(3L);
        assertNotEquals(request, request3);
        
        assertNotNull(request.toString());
        assertNotNull(ExpenseRequest.builder().toString());
    }
    
    @Test
    void testBudgetUpdateRequest() {
        BudgetUpdateRequest request = BudgetUpdateRequest.builder()
            .userId(1L)
            .categoryId(2L)
            .amount(new BigDecimal("100.00"))
            .build();
            
        assertEquals(1L, request.getUserId());
        assertEquals(2L, request.getCategoryId());
        assertEquals(new BigDecimal("100.00"), request.getAmount());
        
        BudgetUpdateRequest request2 = new BudgetUpdateRequest(1L, 2L, new BigDecimal("100.00"));
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        BudgetUpdateRequest request3 = new BudgetUpdateRequest();
        request3.setUserId(2L);
        assertNotEquals(request, request3);
        
        assertNotNull(request.toString());
        assertNotNull(BudgetUpdateRequest.builder().toString());
    }
}
