package com.spendsmart.expense.service.impl;

import com.spendsmart.expense.client.BudgetServiceClient;
import com.spendsmart.expense.client.CategoryServiceClient;
import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.dto.ExpenseResponse;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.exception.ExpenseNotFoundException;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import com.spendsmart.expense.mapper.ExpenseMapper;
import com.spendsmart.expense.repository.ExpenseRepository;
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
@DisplayName("ExpenseServiceImpl Unit Tests")
class ExpenseServiceImplTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private ExpenseMapper expenseMapper;
    @Mock private BudgetServiceClient budgetServiceClient;
    @Mock private CategoryServiceClient categoryServiceClient;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Expense testExpense;
    private ExpenseRequest expenseRequest;
    private final Long userId = 1L;
    private final Long expenseId = 1L;

    @BeforeEach
    void setUp() {
        testExpense = Expense.builder()
                .expenseId(expenseId)
                .userId(userId)
                .categoryId(10L)
                .title("Groceries")
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.now())
                .type(ExpenseType.EXPENSE)
                .build();

        expenseRequest = new ExpenseRequest();
        expenseRequest.setTitle("Groceries");
        expenseRequest.setAmount(new BigDecimal("50.00"));
        expenseRequest.setCategoryId(10L);
    }

    @Test
    @DisplayName("addExpense() - should save and notify budget service")
    void addExpense_ShouldSave() {
        when(expenseMapper.toEntity(any())).thenReturn(testExpense);
        when(expenseRepository.save(any())).thenReturn(testExpense);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        ExpenseResponse response = expenseService.addExpense(userId, expenseRequest);

        assertThat(response).isNotNull();
        verify(expenseRepository).save(any());
        verify(budgetServiceClient).updateSpentAmount(any());
    }

    @Test
    @DisplayName("getExpenseById() - should return when owned by user")
    void getExpenseById_ShouldReturn() {
        when(expenseRepository.findByExpenseId(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseMapper.toResponse(testExpense)).thenReturn(new ExpenseResponse());

        ExpenseResponse response = expenseService.getExpenseById(userId, expenseId);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("getExpenseById() - should throw UnauthorizedAccessException when not owner")
    void getExpenseById_Unauthorized_ShouldThrowException() {
        when(expenseRepository.findByExpenseId(expenseId)).thenReturn(Optional.of(testExpense));

        assertThatThrownBy(() -> expenseService.getExpenseById(99L, expenseId))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("getExpensesByUser() - should return paged results")
    void getExpensesByUser_ShouldReturnPage() {
        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.findByUserId(eq(userId), any())).thenReturn(page);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        Page<ExpenseResponse> result = expenseService.getExpensesByUser(userId, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("updateExpense() - should adjust budget")
    void updateExpense_ShouldUpdateAndAdjustBudget() {
        when(expenseRepository.findByExpenseId(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any())).thenReturn(testExpense);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        expenseRequest.setAmount(new BigDecimal("75.00")); // +25 difference
        ExpenseResponse response = expenseService.updateExpense(userId, expenseId, expenseRequest);

        assertThat(response).isNotNull();
        verify(budgetServiceClient).updateSpentAmount(argThat(req -> req.getAmount().equals(new BigDecimal("25.00"))));
    }

    @Test
    @DisplayName("deleteExpense() - should notify budget service")
    void deleteExpense_ShouldDelete() {
        when(expenseRepository.findByExpenseId(expenseId)).thenReturn(Optional.of(testExpense));

        expenseService.deleteExpense(userId, expenseId);

        verify(expenseRepository).delete(testExpense);
        verify(budgetServiceClient).updateSpentAmount(argThat(req -> req.getAmount().equals(new BigDecimal("-50.00"))));
    }

    @Test
    @DisplayName("updateExpense() - category change should decrement old and increment new budget")
    void updateExpense_CategoryChange_ShouldAdjustBothBudgets() {
        when(expenseRepository.findByExpenseId(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any())).thenReturn(testExpense);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        expenseRequest.setCategoryId(20L); // Changed from 10L
        expenseRequest.setAmount(new BigDecimal("50.00"));

        expenseService.updateExpense(userId, expenseId, expenseRequest);

        // Should decrement old category (10L) by 50 and increment new (20L) by 50
        verify(budgetServiceClient).updateSpentAmount(argThat(req -> req.getCategoryId().equals(10L) && req.getAmount().compareTo(new BigDecimal("-50.00")) == 0));
        verify(budgetServiceClient).updateSpentAmount(argThat(req -> req.getCategoryId().equals(20L) && req.getAmount().compareTo(new BigDecimal("50.00")) == 0));
    }

    @Test
    @DisplayName("getCategoryBreakdown() - should map IDs to names correctly")
    void getCategoryBreakdown_ShouldMapNames() {
        Object[] row = new Object[]{10L, new BigDecimal("100.00")};
        when(expenseRepository.sumAmountByUserIdAndMonthGroupByCategory(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(row));
        
        com.spendsmart.shared.dto.ApiResponse<java.util.Map<Long, String>> apiResponse =
            com.spendsmart.shared.dto.ApiResponse.success("ok", Collections.singletonMap(10L, "Food"));
        when(categoryServiceClient.getCategoryNames()).thenReturn(apiResponse);

        java.util.Map<String, BigDecimal> result = expenseService.getCategoryBreakdown(userId, 2026, 4);

        assertThat(result).containsKey("Food");
        assertThat(result.get("Food")).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("getCategoryBreakdown() - should handle service failure with fallback")
    void getCategoryBreakdown_ServiceFailure_ShouldUseFallback() {
        Object[] row = new Object[]{10L, new BigDecimal("100.00")};
        when(expenseRepository.sumAmountByUserIdAndMonthGroupByCategory(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(row));
        when(categoryServiceClient.getCategoryNames()).thenThrow(new RuntimeException("Service down"));

        java.util.Map<String, BigDecimal> result = expenseService.getCategoryBreakdown(userId, 2026, 4);

        assertThat(result).containsKey("Unknown Category (10)");
        assertThat(result.get("Unknown Category (10)")).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("getExpensesByCategory() - should return paged results")
    void getExpensesByCategory_ShouldReturnPage() {
        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.findByUserIdAndCategoryId(eq(userId), eq(10L), any())).thenReturn(page);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        Page<ExpenseResponse> result = expenseService.getExpensesByCategory(userId, 10L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getExpensesByDateRange() - should return paged results")
    void getExpensesByDateRange_ShouldReturnPage() {
        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        when(expenseRepository.findByUserIdAndDateBetween(eq(userId), eq(start), eq(end), any())).thenReturn(page);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        Page<ExpenseResponse> result = expenseService.getExpensesByDateRange(userId, start, end, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getExpensesByMonth() - should return paged results")
    void getExpensesByMonth_ShouldReturnPage() {
        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.findByUserIdAndMonth(eq(userId), eq(2026), eq(5), any())).thenReturn(page);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        Page<ExpenseResponse> result = expenseService.getExpensesByMonth(userId, 2026, 5, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getExpensesByType() - should return paged results")
    void getExpensesByType_ShouldReturnPage() {
        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.findByUserIdAndType(eq(userId), eq(ExpenseType.EXPENSE), any())).thenReturn(page);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        Page<ExpenseResponse> result = expenseService.getExpensesByType(userId, ExpenseType.EXPENSE, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchExpenses() - should return paged results")
    void searchExpenses_ShouldReturnPage() {
        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.searchByKeyword(eq(userId), eq("Groceries"), any())).thenReturn(page);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        Page<ExpenseResponse> result = expenseService.searchExpenses(userId, "Groceries", PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllExpenses() - should return paged results")
    void getAllExpenses_ShouldReturnPage() {
        Page<Expense> page = new PageImpl<>(Collections.singletonList(testExpense));
        when(expenseRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseResponse());

        Page<ExpenseResponse> result = expenseService.getAllExpenses(PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getGlobalTotalExpenses() - should return total")
    void getGlobalTotalExpenses_ShouldReturnTotal() {
        when(expenseRepository.sumAllExpenses()).thenReturn(new BigDecimal("1000.00"));

        BigDecimal total = expenseService.getGlobalTotalExpenses();

        assertThat(total).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("getGlobalExpenseCount() - should return count")
    void getGlobalExpenseCount_ShouldReturnCount() {
        when(expenseRepository.count()).thenReturn(100L);

        long count = expenseService.getGlobalExpenseCount();

        assertThat(count).isEqualTo(100L);
    }

    @Test
    @DisplayName("getTotalByUser() - should return total")
    void getTotalByUser_ShouldReturnTotal() {
        when(expenseRepository.sumAmountByUserId(userId)).thenReturn(new BigDecimal("500.00"));

        BigDecimal total = expenseService.getTotalByUser(userId);

        assertThat(total).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("getTotalByCategory() - should return total")
    void getTotalByCategory_ShouldReturnTotal() {
        when(expenseRepository.sumAmountByUserIdAndCategoryId(userId, 10L)).thenReturn(new BigDecimal("300.00"));

        BigDecimal total = expenseService.getTotalByCategory(userId, 10L);

        assertThat(total).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("getTotalByMonth() - should return total")
    void getTotalByMonth_ShouldReturnTotal() {
        when(expenseRepository.sumAmountByUserIdAndMonth(userId, 2026, 5)).thenReturn(new BigDecimal("200.00"));

        BigDecimal total = expenseService.getTotalByMonth(userId, 2026, 5);

        assertThat(total).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("getDailyTrend() - should return map")
    void getDailyTrend_ShouldReturnMap() {
        LocalDate today = LocalDate.now();
        Object[] row = new Object[]{today, new BigDecimal("50.00")};
        when(expenseRepository.sumAmountByUserIdAndMonthGroupByDate(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(row));

        java.util.Map<String, BigDecimal> result = expenseService.getDailyTrend(userId, 2026, 5);

        assertThat(result).containsKey(today.toString());
        assertThat(result.get(today.toString())).isEqualByComparingTo("50.00");
    }
}
