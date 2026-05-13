package com.spendsmart.category.exception;

/**
 * Exception thrown when a user tries to create a category that already exists.
 */
public class DuplicateCategoryException extends RuntimeException {
    /**
     * Constructs the exception with a specific error message.
     * @param message The error message.
     */
    public DuplicateCategoryException(String message) {
        super(message);
    }
}
