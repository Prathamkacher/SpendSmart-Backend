package com.spendsmart.income.service.impl;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.dto.IncomeResponse;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.IncomeSource;
import com.spendsmart.income.exception.IncomeNotFoundException;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import com.spendsmart.income.mapper.IncomeMapper;
import com.spendsmart.income.repository.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IncomeServiceImpl Unit Tests")
class IncomeServiceImplTest {

    @Mock private IncomeRepository incomeRepository;
    @Mock private IncomeMapper incomeMapper;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    private Income testIncome;
    private IncomeRequest incomeRequest;
    private final Long USER_ID = 1L;
    private final Long INCOME_ID = 1L;

    @BeforeEach
    void setUp() {
        testIncome = Income.builder()
                .incomeId(INCOME_ID)
                .userId(USER_ID)
                .title("Salary")
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now())
                .source(IncomeSource.SALARY)
                .build();

        incomeRequest = new IncomeRequest();
        incomeRequest.setTitle("Salary");
        incomeRequest.setAmount(new BigDecimal("5000.00"));
        incomeRequest.setSource(IncomeSource.SALARY);
    }

    @Test
    @DisplayName("addIncome() - should save income")
    void addIncome_ShouldSave() {
        when(incomeMapper.toEntity(any())).thenReturn(testIncome);
        when(incomeRepository.save(any())).thenReturn(testIncome);
        when(incomeMapper.toResponse(any())).thenReturn(new IncomeResponse());

        IncomeResponse response = incomeService.addIncome(USER_ID, incomeRequest);

        assertThat(response).isNotNull();
        verify(incomeRepository).save(any());
    }

    @Test
    @DisplayName("getIncomeById() - should return when owned by user")
    void getIncomeById_ShouldReturn() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.of(testIncome));
        when(incomeMapper.toResponse(testIncome)).thenReturn(new IncomeResponse());

        IncomeResponse response = incomeService.getIncomeById(USER_ID, INCOME_ID);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("getIncomeById() - should throw when income is missing")
    void getIncomeById_NotFound_ShouldThrowException() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.getIncomeById(USER_ID, INCOME_ID))
                .isInstanceOf(IncomeNotFoundException.class)
                .hasMessageContaining("Income not found with id: 1");
    }

    @Test
    @DisplayName("getIncomeById() - should throw UnauthorizedAccessException when not owner")
    void getIncomeById_Unauthorized_ShouldThrowException() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.of(testIncome));

        assertThatThrownBy(() -> incomeService.getIncomeById(99L, INCOME_ID))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("updateIncome() - should update when owner")
    void updateIncome_ShouldUpdate() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.of(testIncome));
        when(incomeRepository.save(any())).thenReturn(testIncome);
        when(incomeMapper.toResponse(any())).thenReturn(new IncomeResponse());

        IncomeResponse response = incomeService.updateIncome(USER_ID, INCOME_ID, incomeRequest);

        assertThat(response).isNotNull();
        verify(incomeRepository).save(any());
    }

    @Test
    @DisplayName("updateIncome() - should throw when income is missing")
    void updateIncome_NotFound_ShouldThrowException() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.updateIncome(USER_ID, INCOME_ID, incomeRequest))
                .isInstanceOf(IncomeNotFoundException.class)
                .hasMessageContaining("Income not found with id: 1");
    }

    @Test
    @DisplayName("updateIncome() - should throw UnauthorizedAccessException when not owner")
    void updateIncome_Unauthorized_ShouldThrowException() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.of(testIncome));

        assertThatThrownBy(() -> incomeService.updateIncome(99L, INCOME_ID, incomeRequest))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("permission to update");

        verify(incomeMapper, never()).updateEntity(any(), any());
        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteIncome() - should delete when owner")
    void deleteIncome_ShouldDelete() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.of(testIncome));

        incomeService.deleteIncome(USER_ID, INCOME_ID);

        verify(incomeRepository).delete(testIncome);
    }

    @Test
    @DisplayName("deleteIncome() - should throw when income is missing")
    void deleteIncome_NotFound_ShouldThrowException() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.deleteIncome(USER_ID, INCOME_ID))
                .isInstanceOf(IncomeNotFoundException.class)
                .hasMessageContaining("Income not found with id: 1");
    }

    @Test
    @DisplayName("deleteIncome() - should throw UnauthorizedAccessException when not owner")
    void deleteIncome_Unauthorized_ShouldThrowException() {
        when(incomeRepository.findByIncomeId(INCOME_ID)).thenReturn(Optional.of(testIncome));

        assertThatThrownBy(() -> incomeService.deleteIncome(99L, INCOME_ID))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("permission to delete");

        verify(incomeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getTotalIncomeByUser() - should return repository sum when present")
    void getTotalIncomeByUser_ShouldReturnRepositoryTotal() {
        when(incomeRepository.sumAmountByUserId(USER_ID)).thenReturn(new BigDecimal("8450.50"));

        BigDecimal total = incomeService.getTotalIncomeByUser(USER_ID);

        assertThat(total).isEqualByComparingTo("8450.50");
    }

    @Test
    @DisplayName("getTotalIncomeByUser() - should return ZERO when no income exists")
    void getTotalIncomeByUser_NoIncome_ShouldReturnZero() {
        when(incomeRepository.sumAmountByUserId(USER_ID)).thenReturn(null);

        BigDecimal total = incomeService.getTotalIncomeByUser(USER_ID);

        assertThat(total).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getTotalIncomeByMonth() - should return sum for specific month")
    void getTotalIncomeByMonth_ShouldReturnSum() {
        when(incomeRepository.sumAmountByUserIdAndMonth(USER_ID, 2023, 10)).thenReturn(new BigDecimal("3000.00"));

        BigDecimal total = incomeService.getTotalIncomeByMonth(USER_ID, 2023, 10);

        assertThat(total).isEqualTo(new BigDecimal("3000.00"));
    }

    @Test
    @DisplayName("getTotalIncomeByMonth() - should return ZERO when repository returns null")
    void getTotalIncomeByMonth_Null_ShouldReturnZero() {
        when(incomeRepository.sumAmountByUserIdAndMonth(USER_ID, 2023, 10)).thenReturn(null);

        BigDecimal total = incomeService.getTotalIncomeByMonth(USER_ID, 2023, 10);

        assertThat(total).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getGlobalTotalIncome() - should handle null global sum")
    void getGlobalTotalIncome_Null_ShouldReturnZero() {
        when(incomeRepository.sumAllIncome()).thenReturn(null);

        BigDecimal total = incomeService.getGlobalTotalIncome();

        assertThat(total).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getGlobalTotalIncome() - should return repository sum when present")
    void getGlobalTotalIncome_ShouldReturnRepositoryTotal() {
        when(incomeRepository.sumAllIncome()).thenReturn(new BigDecimal("125000.00"));

        BigDecimal total = incomeService.getGlobalTotalIncome();

        assertThat(total).isEqualByComparingTo("125000.00");
    }

    @Test
    @DisplayName("getGlobalIncomeCount() - should return count")
    void getGlobalIncomeCount_ShouldReturnCount() {
        when(incomeRepository.count()).thenReturn(100L);

        long count = incomeService.getGlobalIncomeCount();

        assertThat(count).isEqualTo(100L);
    }
    @Test
    @DisplayName("getIncomesByUser() - should return paged results")
    void getIncomesByUser_ShouldReturnPage() {
        Page<Income> page = new PageImpl<>(Collections.singletonList(testIncome));
        when(incomeRepository.findByUserId(eq(USER_ID), any())).thenReturn(page);
        when(incomeMapper.toResponse(any())).thenReturn(new IncomeResponse());

        Page<IncomeResponse> result = incomeService.getIncomesByUser(USER_ID, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getIncomesBySource() - should return paged results")
    void getIncomesBySource_ShouldReturnPage() {
        Page<Income> page = new PageImpl<>(Collections.singletonList(testIncome));
        when(incomeRepository.findByUserIdAndSource(eq(USER_ID), eq(IncomeSource.SALARY), any())).thenReturn(page);
        when(incomeMapper.toResponse(any())).thenReturn(new IncomeResponse());

        Page<IncomeResponse> result = incomeService.getIncomesBySource(USER_ID, IncomeSource.SALARY, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getIncomesByDateRange() - should return paged results")
    void getIncomesByDateRange_ShouldReturnPage() {
        Page<Income> page = new PageImpl<>(Collections.singletonList(testIncome));
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        when(incomeRepository.findByUserIdAndDateBetween(eq(USER_ID), eq(start), eq(end), any())).thenReturn(page);
        when(incomeMapper.toResponse(any())).thenReturn(new IncomeResponse());

        Page<IncomeResponse> result = incomeService.getIncomesByDateRange(USER_ID, start, end, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getIncomesByMonth() - should return paged results")
    void getIncomesByMonth_ShouldReturnPage() {
        Page<Income> page = new PageImpl<>(Collections.singletonList(testIncome));
        when(incomeRepository.findByUserIdAndMonth(eq(USER_ID), eq(2023), eq(10), any())).thenReturn(page);
        when(incomeMapper.toResponse(any())).thenReturn(new IncomeResponse());

        Page<IncomeResponse> result = incomeService.getIncomesByMonth(USER_ID, 2023, 10, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getRecurringIncomes() - should return list")
    void getRecurringIncomes_ShouldReturnList() {
        when(incomeRepository.findByIsRecurringTrue()).thenReturn(Collections.singletonList(testIncome));
        when(incomeMapper.toResponse(any())).thenReturn(new IncomeResponse());

        java.util.List<IncomeResponse> result = incomeService.getRecurringIncomes();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllIncomes() - should return paged results")
    void getAllIncomes_ShouldReturnPage() {
        Page<Income> page = new PageImpl<>(Collections.singletonList(testIncome));
        when(incomeRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(incomeMapper.toResponse(any())).thenReturn(new IncomeResponse());

        Page<IncomeResponse> result = incomeService.getAllIncomes(PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }
}
