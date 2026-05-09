package com.spendsmart.expense.service;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.dto.ExpenseResponse;
import com.spendsmart.expense.entity.ExpenseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Expense business logic contract.
 */
public interface ExpenseService {

    ExpenseResponse addExpense(Long userId, ExpenseRequest request);

    ExpenseResponse getExpenseById(Long userId, Long expenseId);

    Page<ExpenseResponse> getExpensesByUser(Long userId, Pageable pageable);

    Page<ExpenseResponse> getExpensesByCategory(Long userId, Long categoryId, Pageable pageable);

    Page<ExpenseResponse> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<ExpenseResponse> getExpensesByMonth(Long userId, int year, int month, Pageable pageable);

    ExpenseResponse updateExpense(Long userId, Long expenseId, ExpenseRequest request);

    void deleteExpense(Long userId, Long expenseId);

    BigDecimal getTotalByUser(Long userId);

    BigDecimal getTotalByCategory(Long userId, Long categoryId);

    BigDecimal getTotalByMonth(Long userId, int year, int month);

    java.util.Map<String, BigDecimal> getCategoryBreakdown(Long userId, int year, int month);

    java.util.Map<String, BigDecimal> getDailyTrend(Long userId, int year, int month);

    Page<ExpenseResponse> getExpensesByType(Long userId, ExpenseType type, Pageable pageable);

    Page<ExpenseResponse> searchExpenses(Long userId, String keyword, Pageable pageable);

    // Admin methods
    Page<ExpenseResponse> getAllExpenses(Pageable pageable);

    BigDecimal getGlobalTotalExpenses();

    long getGlobalExpenseCount();
}
