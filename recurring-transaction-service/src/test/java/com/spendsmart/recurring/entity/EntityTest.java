package com.spendsmart.recurring.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testRecurringTransaction() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        RecurringTransaction entity = new RecurringTransaction();
        entity.setRecurringId(1L);
        entity.setUserId(2L);
        entity.setCategoryId(3L);
        entity.setTitle("Rent");
        entity.setAmount(new BigDecimal("1000.00"));
        entity.setType(TransactionType.EXPENSE);
        entity.setFrequency(Frequency.MONTHLY);
        entity.setStartDate(today);
        entity.setEndDate(today.plusYears(1));
        entity.setNextDueDate(today.plusMonths(1));
        entity.setIsActive(true);
        entity.setDescription("Desc");
        entity.setPaymentMethod(PaymentMethod.BANK);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertEquals(1L, entity.getRecurringId());
        assertEquals(2L, entity.getUserId());
        assertEquals(3L, entity.getCategoryId());
        assertEquals("Rent", entity.getTitle());
        assertEquals(new BigDecimal("1000.00"), entity.getAmount());
        assertEquals(TransactionType.EXPENSE, entity.getType());
        assertEquals(Frequency.MONTHLY, entity.getFrequency());
        assertEquals(today, entity.getStartDate());
        assertEquals(today.plusYears(1), entity.getEndDate());
        assertEquals(today.plusMonths(1), entity.getNextDueDate());
        assertTrue(entity.getIsActive());
        assertEquals("Desc", entity.getDescription());
        assertEquals(PaymentMethod.BANK, entity.getPaymentMethod());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());

        RecurringTransaction entity2 = new RecurringTransaction(1L, 2L, 3L, "Rent", new BigDecimal("1000.00"), TransactionType.EXPENSE, Frequency.MONTHLY, today, today.plusYears(1), today.plusMonths(1), true, "Desc", PaymentMethod.BANK, now, now);
        assertEquals(1L, entity2.getRecurringId());
        
        RecurringTransaction entity4 = RecurringTransaction.builder()
            .recurringId(1L)
            .userId(2L)
            .categoryId(3L)
            .title("Rent")
            .amount(new BigDecimal("1000.00"))
            .type(TransactionType.EXPENSE)
            .frequency(Frequency.MONTHLY)
            .startDate(today)
            .endDate(today.plusYears(1))
            .nextDueDate(today.plusMonths(1))
            .isActive(true)
            .description("Desc")
            .paymentMethod(PaymentMethod.BANK)
            .createdAt(now)
            .updatedAt(now)
            .build();
            
        assertEquals(1L, entity4.getRecurringId());
        assertNotNull(RecurringTransaction.builder().toString());
    }

    @Test
    void testEnums() {
        Frequency[] freqValues = Frequency.values();
        assertTrue(freqValues.length > 0);
        assertEquals(Frequency.MONTHLY, Frequency.valueOf("MONTHLY"));
        
        TransactionType[] typeValues = TransactionType.values();
        assertTrue(typeValues.length > 0);
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"));
        
        PaymentMethod[] methodValues = PaymentMethod.values();
        assertTrue(methodValues.length > 0);
        assertEquals(PaymentMethod.BANK, PaymentMethod.valueOf("BANK"));
    }
}
