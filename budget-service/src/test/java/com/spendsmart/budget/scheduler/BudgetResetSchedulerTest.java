package com.spendsmart.budget.scheduler;

import com.spendsmart.budget.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BudgetResetSchedulerTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private BudgetResetScheduler budgetResetScheduler;

    @Test
    void resetBudgets_ShouldDelegateToService() {
        budgetResetScheduler.resetBudgets();

        verify(budgetService).resetExpiredBudgets();
    }

    @Test
    void resetBudgets_ShouldSwallowFailures() {
        doThrow(new RuntimeException("scheduler error")).when(budgetService).resetExpiredBudgets();

        budgetResetScheduler.resetBudgets();

        verify(budgetService).resetExpiredBudgets();
    }
}
