package com.spendsmart.category.exception;

/**
 * Exception thrown when a category cannot be found.
 */
public class CategoryNotFoundException extends RuntimeException {
    /**
     * Constructs the exception with a message containing the category ID.
     * @param id The ID of the category that was not found.
     */
    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id);
    }
}
