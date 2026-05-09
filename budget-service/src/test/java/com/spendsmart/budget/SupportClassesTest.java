package com.spendsmart.budget;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.entity.BudgetPeriod;
import com.spendsmart.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SupportClassesTest {

    @Test
    void apiResponseFactories_ShouldPopulateFlagsAndTimestamp() {
        ApiResponse<String> success = ApiResponse.success("saved", "payload");
        ApiResponse<Void> error = ApiResponse.error("broken");

        assertThat(success.isSuccess()).isTrue();
        assertThat(success.getData()).isEqualTo("payload");
        assertThat(success.getTimestamp()).isNotNull();
        assertThat(error.isSuccess()).isFalse();
        assertThat(error.getMessage()).isEqualTo("broken");
    }

    @Test
    void budgetBuilder_ShouldApplyDefaults() {
        Budget budget = Budget.builder()
                .budgetId(1L)
                .userId(2L)
                .categoryId(3L)
                .name("Essentials")
                .limitAmount(new BigDecimal("1200.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();

        assertThat(budget.getCurrency()).isEqualTo("INR");
        assertThat(budget.getPeriod()).isEqualTo(BudgetPeriod.MONTHLY);
        assertThat(budget.getSpentAmount()).isEqualByComparingTo("0");
        assertThat(budget.getAlertThreshold()).isEqualTo(80);
        assertThat(budget.getIsActive()).isTrue();
    }
}
