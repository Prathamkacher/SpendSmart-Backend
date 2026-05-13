package com.spendsmart.income.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested income record cannot be found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class IncomeNotFoundException extends RuntimeException {
    public IncomeNotFoundException(String message) {
        super(message);
    }
}
