package com.spendsmart.expense.service.impl;

import com.spendsmart.expense.client.BudgetServiceClient;
import com.spendsmart.expense.constants.AppConstants;
import com.spendsmart.expense.dto.BudgetUpdateRequest;
import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.dto.ExpenseResponse;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.exception.ExpenseNotFoundException;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import com.spendsmart.expense.mapper.ExpenseMapper;
import com.spendsmart.expense.repository.ExpenseRepository;
import com.spendsmart.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Implementation of the {@link ExpenseService}.
 * Manages the persistence of expense records and coordinates with the {@link BudgetServiceClient}
 * to ensure spending limits are updated in real-time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository     expenseRepository;
    private final ExpenseMapper         expenseMapper;
    private final BudgetServiceClient   budgetServiceClient;
    private final com.spendsmart.expense.client.CategoryServiceClient categoryServiceClient;

    // ── CREATE ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public ExpenseResponse addExpense(Long userId, ExpenseRequest request) {
        log.info("Creating expense for userId={}, title='{}'", userId, request.getTitle());

        Expense expense = expenseMapper.toEntity(request);
        expense.setUserId(userId);

        // Default isRecurring to false if not provided
        if (expense.getIsRecurring() == null) {
            expense.setIsRecurring(false);
        }

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created: id={}, amount={}", saved.getExpenseId(), saved.getAmount());

        // Notify Budget-Service: increment spentAmount
        updateBudgetService(userId, saved.getCategoryId(), saved.getAmount());

        return expenseMapper.toResponse(saved);
    }

    // ── READ ─────────────────────────────────────────────────────────

    @Override
    public ExpenseResponse getExpenseById(Long userId, Long expenseId) {
        Expense expense = findExpenseOrThrow(expenseId);
        verifyOwnership(expense, userId);
        return expenseMapper.toResponse(expense);
    }

    @Override
    public Page<ExpenseResponse> getExpensesByUser(Long userId, Pageable pageable) {
        log.debug("Fetching expenses for userId={}, page={}", userId, pageable.getPageNumber());
        return expenseRepository.findByUserId(userId, pageable)
                .map(expenseMapper::toResponse);
    }

    @Override
    public Page<ExpenseResponse> getExpensesByCategory(Long userId, Long categoryId, Pageable pageable) {
        log.debug("Fetching expenses for userId={}, categoryId={}", userId, categoryId);
        return expenseRepository.findByUserIdAndCategoryId(userId, categoryId, pageable)
                .map(expenseMapper::toResponse);
    }

    @Override
    public Page<ExpenseResponse> getExpensesByDateRange(Long userId, LocalDate startDate,
                                                         LocalDate endDate, Pageable pageable) {
        log.debug("Fetching expenses for userId={}, dateRange=[{}, {}]", userId, startDate, endDate);
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate, pageable)
                .map(expenseMapper::toResponse);
    }

    @Override
    public Page<ExpenseResponse> getExpensesByMonth(Long userId, int year, int month, Pageable pageable) {
        log.debug("Fetching expenses for userId={}, month={}/{}", userId, year, month);
        return expenseRepository.findByUserIdAndMonth(userId, year, month, pageable)
                .map(expenseMapper::toResponse);
    }

    @Override
    public Page<ExpenseResponse> getExpensesByType(Long userId, ExpenseType type, Pageable pageable) {
        log.debug("Fetching expenses for userId={}, type={}", userId, type);
        return expenseRepository.findByUserIdAndType(userId, type, pageable)
                .map(expenseMapper::toResponse);
    }

    @Override
    public Page<ExpenseResponse> searchExpenses(Long userId, String keyword, Pageable pageable) {
        log.debug("Searching expenses for userId={}, keyword='{}'", userId, keyword);
        return expenseRepository.searchByKeyword(userId, keyword, pageable)
                .map(expenseMapper::toResponse);
    }

    // Admin methods
    @Override
    public Page<ExpenseResponse> getAllExpenses(Pageable pageable) {
        log.info("Admin: Fetching all platform expenses");
        return expenseRepository.findAll(pageable)
                .map(expenseMapper::toResponse);
    }

    @Override
    public BigDecimal getGlobalTotalExpenses() {
        log.info("Admin: Calculating global total expenses");
        BigDecimal total = expenseRepository.sumAllExpenses();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public long getGlobalExpenseCount() {
        log.info("Admin: Counting global expenses");
        return expenseRepository.count();
    }

    // ── AGGREGATIONS ─────────────────────────────────────────────────

    @Override
    public BigDecimal getTotalByUser(Long userId) {
        log.debug("Calculating total for userId={}", userId);
        BigDecimal total = expenseRepository.sumAmountByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalByCategory(Long userId, Long categoryId) {
        log.debug("Calculating total for userId={}, categoryId={}", userId, categoryId);
        BigDecimal total = expenseRepository.sumAmountByUserIdAndCategoryId(userId, categoryId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalByMonth(Long userId, int year, int month) {
        log.debug("Calculating total for userId={}, month={}/{}", userId, year, month);
        BigDecimal total = expenseRepository.sumAmountByUserIdAndMonth(userId, year, month);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public java.util.Map<String, BigDecimal> getCategoryBreakdown(Long userId, int year, int month) {
        log.debug("Calculating breakdown for userId={}, month={}/{}", userId, year, month);
        
        java.util.List<Object[]> results = expenseRepository.sumAmountByUserIdAndMonthGroupByCategory(userId, year, month);
        java.util.Map<Long, String> categoryNames = fetchCategoryNames();
        
        java.util.Map<String, BigDecimal> breakdown = new java.util.HashMap<>();
        for (Object[] res : results) {
            if (res == null || res.length < 2) continue;
            Long categoryId = (Long) res[0];
            BigDecimal amount = (BigDecimal) res[1];
            String categoryName = categoryNames.getOrDefault(categoryId, AppConstants.UNKNOWN_CATEGORY + " (" + categoryId + ")");
            breakdown.put(categoryName, amount != null ? amount : BigDecimal.ZERO);
        }
        return breakdown;
    }

    @Override
    public java.util.Map<String, BigDecimal> getDailyTrend(Long userId, int year, int month) {
        log.debug("Calculating daily trend for userId={}, month={}/{}", userId, year, month);
        
        java.util.List<Object[]> results = expenseRepository.sumAmountByUserIdAndMonthGroupByDate(userId, year, month);
        java.util.Map<String, BigDecimal> trend = new java.util.TreeMap<>();
        for (Object[] res : results) {
            LocalDate date = (LocalDate) res[0];
            BigDecimal amount = (BigDecimal) res[1];
            trend.put(date.toString(), amount);
        }
        return trend;
    }

    private java.util.Map<Long, String> fetchCategoryNames() {
        try {
            com.spendsmart.shared.dto.ApiResponse<java.util.Map<Long, String>> response = categoryServiceClient.getCategoryNames();
            if (response != null && response.isSuccess()) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch category names: {}", e.getMessage());
        }
        return java.util.Collections.emptyMap();
    }

    // ── UPDATE ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long userId, Long expenseId, ExpenseRequest request) {
        log.info("Updating expense id={} for userId={}", expenseId, userId);

        Expense existing = findExpenseOrThrow(expenseId);
        verifyOwnership(existing, userId);

        // Calculate amount difference for budget adjustment
        BigDecimal oldAmount    = existing.getAmount();
        Long       oldCategory  = existing.getCategoryId();
        BigDecimal newAmount    = request.getAmount();
        Long       newCategory  = request.getCategoryId();

        // Update entity fields from request
        expenseMapper.updateEntityFromRequest(request, existing);
        Expense updated = expenseRepository.save(existing);
        log.info("Expense updated: id={}, oldAmount={}, newAmount={}", expenseId, oldAmount, newAmount);

        // Adjust Budget-Service:
        // If category changed: decrement old category, increment new category
        // If same category: adjust by difference
        if (oldCategory != null && oldCategory.equals(newCategory)) {
            BigDecimal diff = newAmount.subtract(oldAmount);
            if (diff.compareTo(BigDecimal.ZERO) != 0) {
                updateBudgetService(userId, newCategory, diff);
            }
        } else {
            // Decrement old category
            if (oldCategory != null) {
                updateBudgetService(userId, oldCategory, oldAmount.negate());
            }
            // Increment new category
            if (newCategory != null) {
                updateBudgetService(userId, newCategory, newAmount);
            }
        }

        return expenseMapper.toResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteExpense(Long userId, Long expenseId) {
        log.info("Deleting expense id={} for userId={}", expenseId, userId);

        Expense expense = findExpenseOrThrow(expenseId);
        verifyOwnership(expense, userId);

        // Notify Budget-Service: decrement spentAmount
        updateBudgetService(userId, expense.getCategoryId(), expense.getAmount().negate());

        expenseRepository.delete(expense);
        log.info("Expense deleted: id={}", expenseId);
    }

    // ── Private helpers ──────────────────────────────────────────────

    /**
     * Find expense by ID or throw 404.
     */
    private Expense findExpenseOrThrow(Long expenseId) {
        return expenseRepository.findByExpenseId(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
    }

    /**
     * Verify the authenticated user owns this expense.
     */
    private void verifyOwnership(Expense expense, Long userId) {
        if (!expense.getUserId().equals(userId)) {
            log.warn("Unauthorized access: userId={} tried to access expense owned by userId={}",
                    userId, expense.getUserId());
            throw new UnauthorizedAccessException(AppConstants.UNAUTHORIZED_ACCESS);
        }
    }

    /**
     * Call Budget-Service to update spent amount.
     */
    private void updateBudgetService(Long userId, Long categoryId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        try {
            com.spendsmart.expense.dto.BudgetUpdateRequest budgetRequest = com.spendsmart.expense.dto.BudgetUpdateRequest.builder()
                    .userId(userId)
                    .categoryId(categoryId)
                    .amount(amount)
                    .build();

            budgetServiceClient.updateSpentAmount(budgetRequest);
            log.info("Budget-Service updated: userId={}, categoryId={}, amount={}", userId, categoryId, amount);
        } catch (Exception ex) {
            log.warn("Failed to update Budget-Service: {}", ex.getMessage());
        }
    }
}
