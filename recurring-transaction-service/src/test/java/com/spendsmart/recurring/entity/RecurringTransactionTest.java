package com.spendsmart.recurring.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class RecurringTransactionTest {

    @Test
    void testRecurringTransactionEntity() {
        RecurringTransaction transaction = RecurringTransaction.builder()
                .recurringId(1L)
                .userId(1L)
                .categoryId(1L)
                .title("Title")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.EXPENSE)
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .nextDueDate(LocalDate.now())
                .isActive(true)
                .description("Description")
                .paymentMethod(PaymentMethod.CASH)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(transaction.getRecurringId()).isEqualTo(1L);
        assertThat(transaction.getUserId()).isEqualTo(1L);
        assertThat(transaction.getCategoryId()).isEqualTo(1L);
        assertThat(transaction.getTitle()).isEqualTo("Title");
        assertThat(transaction.getAmount()).isEqualByComparingTo("100.00");
        assertThat(transaction.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.getFrequency()).isEqualTo(Frequency.MONTHLY);
        assertThat(transaction.getIsActive()).isTrue();
        assertThat(transaction.getDescription()).isEqualTo("Description");
        assertThat(transaction.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);

        transaction.setIsActive(false);
        assertThat(transaction.getIsActive()).isFalse();
    }
}
