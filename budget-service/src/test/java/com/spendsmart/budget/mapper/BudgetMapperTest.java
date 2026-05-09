package com.spendsmart.budget.mapper;

import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.BudgetResponse;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.entity.BudgetPeriod;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetMapperTest {

    private final BudgetMapper budgetMapper = Mappers.getMapper(BudgetMapper.class);

    @Test
    void toEntity_ShouldIgnoreManagedFields() {
        BudgetRequest request = BudgetRequest.builder()
                .name("Food")
                .categoryId(8L)
                .limitAmount(new BigDecimal("2000.00"))
                .currency("INR")
                .period(BudgetPeriod.MONTHLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .alertThreshold(75)
                .build();

        Budget budget = budgetMapper.toEntity(request);

        assertThat(budget.getName()).isEqualTo("Food");
        assertThat(budget.getCategoryId()).isEqualTo(8L);
        assertThat(budget.getUserId()).isNull();
        assertThat(budget.getSpentAmount()).isEqualByComparingTo("0");
        assertThat(budget.getIsActive()).isTrue();
    }

    @Test
    void toResponse_ShouldMapBaseFields() {
        Budget budget = Budget.builder()
                .budgetId(1L)
                .name("Food")
                .categoryId(8L)
                .limitAmount(new BigDecimal("1000.00"))
                .spentAmount(new BigDecimal("850.00"))
                .alertThreshold(80)
                .period(BudgetPeriod.MONTHLY)
                .currency("INR")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();

        BudgetResponse response = budgetMapper.toResponse(budget);

        assertThat(response.getBudgetId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Food");
        assertThat(response.getLimitAmount()).isEqualByComparingTo("1000.00");
    }

    @Test
    void calculateFields_ShouldPopulateDerivedFields() {
        Budget budget = Budget.builder()
                .budgetId(1L)
                .name("Food")
                .categoryId(8L)
                .limitAmount(new BigDecimal("1000.00"))
                .spentAmount(new BigDecimal("850.00"))
                .alertThreshold(80)
                .period(BudgetPeriod.MONTHLY)
                .currency("INR")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();
        BudgetResponse response = new BudgetResponse();

        budgetMapper.calculateFields(budget, response);

        assertThat(response.getProgressPercentage()).isEqualTo(85.0);
        assertThat(response.getRemainingAmount()).isEqualByComparingTo("150.00");
        assertThat(response.getStatus()).isEqualTo("WARNING");
    }

    @Test
    void updateEntityFromRequest_ShouldIgnoreNullsAndManagedFields() {
        BudgetRequest request = BudgetRequest.builder()
                .name("Updated Name")
                .limitAmount(new BigDecimal("1500.00"))
                .build();
        Budget budget = Budget.builder()
                .budgetId(2L)
                .userId(11L)
                .name("Old Name")
                .limitAmount(new BigDecimal("900.00"))
                .spentAmount(new BigDecimal("50.00"))
                .isActive(true)
                .build();

        budgetMapper.updateEntityFromRequest(request, budget);

        assertThat(budget.getName()).isEqualTo("Updated Name");
        assertThat(budget.getLimitAmount()).isEqualByComparingTo("1500.00");
        assertThat(budget.getUserId()).isEqualTo(11L);
        assertThat(budget.getSpentAmount()).isEqualByComparingTo("50.00");
        assertThat(budget.getIsActive()).isTrue();
    }
}
