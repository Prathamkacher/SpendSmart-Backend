package com.spendsmart.shared.exception;
/**
 * Exception thrown when a user attempts to access a resource without sufficient permissions.
 * Extends RuntimeException for unchecked exception handling.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
