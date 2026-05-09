package com.spendsmart.analytics.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class FinancialSnapshotTest {

    @Test
    void testFinancialSnapshotEntity() {
        FinancialSnapshot snapshot = FinancialSnapshot.builder()
                .snapshotId(1L)
                .userId(1L)
                .period("MONTHLY")
                .year(2026)
                .month(4)
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpenses(new BigDecimal("3000.00"))
                .netSavings(new BigDecimal("2000.00"))
                .savingsRate(new BigDecimal("40.00"))
                .topCategory("Food")
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(snapshot.getSnapshotId()).isEqualTo(1L);
        assertThat(snapshot.getUserId()).isEqualTo(1L);
        assertThat(snapshot.getPeriod()).isEqualTo("MONTHLY");
        assertThat(snapshot.getYear()).isEqualTo(2026);
        assertThat(snapshot.getMonth()).isEqualTo(4);
        assertThat(snapshot.getTotalIncome()).isEqualByComparingTo("5000.00");
        assertThat(snapshot.getTopCategory()).isEqualTo("Food");

        snapshot.setTopCategory("Rent");
        assertThat(snapshot.getTopCategory()).isEqualTo("Rent");
    }
}
