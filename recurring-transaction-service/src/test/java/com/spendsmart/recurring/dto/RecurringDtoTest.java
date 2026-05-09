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
        assertEquals("Internet", response.getTitle());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals(Frequency.MONTHLY, response.getFrequency());
        assertTrue(response.getIsActive());
        assertEquals(PaymentMethod.BANK, response.getPaymentMethod());

        RecurringResponse empty = new RecurringResponse();
        assertNotNull(empty.toString());
    }

    @Test
    void testRecurringRequest() {
        RecurringRequest request = new RecurringRequest();
        request.setCategoryId(2L);
        request.setTitle("Netflix");
        request.setAmount(new BigDecimal("15.99"));
        request.setType(TransactionType.EXPENSE);
        request.setFrequency(Frequency.MONTHLY);
        request.setStartDate(LocalDate.now());

        assertEquals("Netflix", request.getTitle());
        assertEquals(new BigDecimal("15.99"), request.getAmount());
        
        assertNotNull(request.toString());
    }

    @Test
    void testExpenseRequest() {
        ExpenseRequest request = new ExpenseRequest();
        request.setCategoryId(1L);
        request.setTitle("Test");
        request.setAmount(new BigDecimal("10.00"));
        assertEquals("Test", request.getTitle());
        assertNotNull(request.toString());
    }

    @Test
    void testIncomeRequest() {
        IncomeRequest request = new IncomeRequest();
        request.setSource("Test");
        request.setAmount(new BigDecimal("10.00"));
        assertEquals("Test", request.getSource());
        assertNotNull(request.toString());
    }

    @Test
    void testNotificationRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("Test");
        assertEquals("Test", request.getTitle());
        assertNotNull(request.toString());
    }
}
