package com.spendsmart.budget.exception;

/**
 * Exception thrown when a requested budget cannot be found in the system.
 */
public class BudgetNotFoundException extends RuntimeException {
    public BudgetNotFoundException(Long id) {
        super("Budget not found with id: " + id);
    }
}
