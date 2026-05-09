package com.spendsmart.budget.service;

import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.BudgetResponse;
import com.spendsmart.budget.dto.BudgetUpdateRequest;

import java.util.List;

public interface BudgetService {
    BudgetResponse createBudget(Long userId, BudgetRequest request);
    BudgetResponse getBudgetById(Long userId, Long budgetId);
    List<BudgetResponse> getBudgetsByUser(Long userId);
    List<BudgetResponse> getActiveBudgets(Long userId);
    BudgetResponse updateBudget(Long userId, Long budgetId, BudgetRequest request);
    void deleteBudget(Long userId, Long budgetId);
    
    // Inter-service sync
    void updateSpentAmount(BudgetUpdateRequest updateRequest);
    
    // Reset and Maintenance
    void resetExpiredBudgets();

    java.math.BigDecimal getTotalBudgetByMonth(Long userId, int year, int month);
}
