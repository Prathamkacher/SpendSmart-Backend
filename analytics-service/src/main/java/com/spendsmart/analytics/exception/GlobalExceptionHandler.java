package com.spendsmart.analytics.exception;

import com.spendsmart.shared.exception.BaseGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central exception handler for the Analytics service.
 * Inherits standardized exception handling from {@link BaseGlobalExceptionHandler}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
}
