package com.spendsmart.category.exception;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.exception.BaseGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the Category service.
 * Intercepts specific exceptions and returns standardized error responses.
 * Extends {@link BaseGlobalExceptionHandler} for common exception handling.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    /**
     * Handles {@link CategoryNotFoundException}.
     * Returns a 404 Not Found response.
     *
     * @param ex The exception.
     * @return Error response.
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFound(CategoryNotFoundException ex) {
        log.warn("Category not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles {@link DuplicateCategoryException}.
     * Returns a 409 Conflict response.
     *
     * @param ex The exception.
     * @return Error response.
     */
    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateCategory(DuplicateCategoryException ex) {
        log.warn("Duplicate category: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
