package com.spendsmart.expense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested expense cannot be found in the system.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(String message) {
        super(message);
    }

    public ExpenseNotFoundException(Long expenseId) {
        super("Expense not found with id: " + expenseId);
    }
}
