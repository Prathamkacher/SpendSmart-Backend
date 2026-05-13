package com.spendsmart.shared.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Extends RuntimeException for unchecked exception handling.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
