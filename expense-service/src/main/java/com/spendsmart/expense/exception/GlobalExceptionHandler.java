package com.spendsmart.expense.exception;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.exception.BaseGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    // Expense not found
    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpenseNotFound(ExpenseNotFoundException ex) {
        log.warn("Expense not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
