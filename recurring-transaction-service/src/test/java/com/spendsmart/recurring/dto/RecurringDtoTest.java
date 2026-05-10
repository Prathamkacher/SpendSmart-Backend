package com.spendsmart.recurring.dto;

import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.PaymentMethod;
import com.spendsmart.recurring.entity.TransactionType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class RecurringDtoTest {

    @Test
    void testRecurringResponse() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        RecurringResponse response = RecurringResponse.builder()
                .recurringId(1L)
                .userId(100L)
                .categoryId(5L)
                .title("Internet")
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .frequency(Frequency.MONTHLY)
                .startDate(today)
                .endDate(today.plusYears(1))
                .nextDueDate(today.plusMonths(1))
                .isActive(true)
                .description("Monthly internet bill")
                .paymentMethod(PaymentMethod.BANK)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(1L, response.getRecurringId());
        assertEquals(100L, response.getUserId());
        assertEquals(5L, response.getCategoryId());
        assertEquals("Internet", response.getTitle());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals(TransactionType.EXPENSE, response.getType());
        assertEquals(Frequency.MONTHLY, response.getFrequency());
        assertEquals(today, response.getStartDate());
        assertEquals(today.plusYears(1), response.getEndDate());
        assertEquals(today.plusMonths(1), response.getNextDueDate());
        assertTrue(response.getIsActive());
        assertEquals("Monthly internet bill", response.getDescription());
        assertEquals(PaymentMethod.BANK, response.getPaymentMethod());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());

        RecurringResponse response2 = new RecurringResponse(1L, 100L, 5L, "Internet", new BigDecimal("50.00"), TransactionType.EXPENSE, Frequency.MONTHLY, today, today.plusYears(1), today.plusMonths(1), true, "Monthly internet bill", PaymentMethod.BANK, now, now);
        
        assertEquals(response, response2);
        assertEquals(response.hashCode(), response2.hashCode());
        
        RecurringResponse response3 = new RecurringResponse();
        response3.setRecurringId(2L);
        assertNotEquals(response, response3);

        assertNotNull(response.toString());
        assertNotNull(RecurringResponse.builder().toString());
    }

    @Test
    void testRecurringRequest() {
        LocalDate today = LocalDate.now();
        RecurringRequest request = RecurringRequest.builder()
            .categoryId(2L)
            .title("Netflix")
            .amount(new BigDecimal("15.99"))
            .type(TransactionType.EXPENSE)
            .frequency(Frequency.MONTHLY)
            .startDate(today)
            .endDate(today.plusYears(1))
            .description("Desc")
            .paymentMethod(PaymentMethod.BANK)
            .build();

        assertEquals(2L, request.getCategoryId());
        assertEquals("Netflix", request.getTitle());
        assertEquals(new BigDecimal("15.99"), request.getAmount());
        assertEquals(TransactionType.EXPENSE, request.getType());
        assertEquals(Frequency.MONTHLY, request.getFrequency());
        assertEquals(today, request.getStartDate());
        assertEquals(today.plusYears(1), request.getEndDate());
        assertEquals("Desc", request.getDescription());
        assertEquals(PaymentMethod.BANK, request.getPaymentMethod());

        RecurringRequest request2 = new RecurringRequest(2L, "Netflix", new BigDecimal("15.99"), TransactionType.EXPENSE, Frequency.MONTHLY, today, today.plusYears(1), "Desc", PaymentMethod.BANK);
        
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        RecurringRequest request3 = new RecurringRequest();
        request3.setCategoryId(3L);
        assertNotEquals(request, request3);
        
        assertNotNull(request.toString());
        assertNotNull(RecurringRequest.builder().toString());
    }

    @Test
    void testExpenseRequest() {
        LocalDate today = LocalDate.now();
        ExpenseRequest request = ExpenseRequest.builder()
            .categoryId(1L)
            .title("Test")
            .amount(new BigDecimal("10.00"))
            .currency("INR")
            .type("EXPENSE")
            .paymentMethod(PaymentMethod.BANK)
            .date(today)
            .notes("Notes")
            .isRecurring(true)
            .build();

        assertEquals(1L, request.getCategoryId());
        assertEquals("Test", request.getTitle());
        assertEquals(new BigDecimal("10.00"), request.getAmount());
        assertEquals("INR", request.getCurrency());
        assertEquals("EXPENSE", request.getType());
        assertEquals(PaymentMethod.BANK, request.getPaymentMethod());
        assertEquals(today, request.getDate());
        assertEquals("Notes", request.getNotes());
        assertTrue(request.getIsRecurring());
            
        ExpenseRequest request2 = new ExpenseRequest(1L, "Test", new BigDecimal("10.00"), "INR", "EXPENSE", PaymentMethod.BANK, today, "Notes", true);
        
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        ExpenseRequest request3 = new ExpenseRequest();
        request3.setCategoryId(2L);
        assertNotEquals(request, request3);
        
        assertNotNull(request.toString());
        assertNotNull(ExpenseRequest.builder().toString());
    }

    @Test
    void testIncomeRequest() {
        LocalDate today = LocalDate.now();
        IncomeRequest request = IncomeRequest.builder()
            .categoryId(1L)
            .title("Test")
            .amount(new BigDecimal("10.00"))
            .currency("INR")
            .source("SALARY")
            .date(today)
            .notes("Notes")
            .isRecurring(true)
            .build();

        assertEquals(1L, request.getCategoryId());
        assertEquals("Test", request.getTitle());
        assertEquals(new BigDecimal("10.00"), request.getAmount());
        assertEquals("INR", request.getCurrency());
        assertEquals("SALARY", request.getSource());
        assertEquals(today, request.getDate());
        assertEquals("Notes", request.getNotes());
        assertTrue(request.getIsRecurring());
            
        IncomeRequest request2 = new IncomeRequest(1L, "Test", new BigDecimal("10.00"), "INR", "SALARY", today, "Notes", true);
        
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        IncomeRequest request3 = new IncomeRequest();
        request3.setCategoryId(2L);
        assertNotEquals(request, request3);
        
        assertNotNull(request.toString());
        assertNotNull(IncomeRequest.builder().toString());
    }

    @Test
    void testNotificationRequest() {
        NotificationRequest request = NotificationRequest.builder()
            .recipientId(1L)
            .type("RECURRING_DUE")
            .severity("INFO")
            .title("Test")
            .message("Message")
            .relatedId(2L)
            .relatedType("RECURRING")
            .build();

        assertEquals(1L, request.getRecipientId());
        assertEquals("RECURRING_DUE", request.getType());
        assertEquals("INFO", request.getSeverity());
        assertEquals("Test", request.getTitle());
        assertEquals("Message", request.getMessage());
        assertEquals(2L, request.getRelatedId());
        assertEquals("RECURRING", request.getRelatedType());
            
        NotificationRequest request2 = new NotificationRequest(1L, "RECURRING_DUE", "INFO", "Test", "Message", 2L, "RECURRING");
        
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        NotificationRequest request3 = new NotificationRequest();
        request3.setRecipientId(2L);
        assertNotEquals(request, request3);
        
        assertNotNull(request.toString());
        assertNotNull(NotificationRequest.builder().toString());
    }
}
