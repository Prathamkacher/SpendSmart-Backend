package com.spendsmart.income.exception;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.exception.BaseGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central exception handler for the Income service.
 * Handles domain-specific income exceptions and integrates with {@link BaseGlobalExceptionHandler}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(IncomeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleIncomeNotFound(IncomeNotFoundException ex) {
        log.warn("Income not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
