package com.spendsmart.expense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(String message) {
        super(message);
    }

    public ExpenseNotFoundException(Long expenseId) {
        super("Expense not found with id: " + expenseId);
    }
}
