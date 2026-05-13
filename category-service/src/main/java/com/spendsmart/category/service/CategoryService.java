package com.spendsmart.category.service;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.dto.CategoryResponse;
import com.spendsmart.category.entity.CategoryType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for managing categories.
 * Defines the contract for category-related operations.
 */
public interface CategoryService {

    /**
     * Creates a new category for the specified user.
     *
     * @param userId The ID of the user.
     * @param request The category details.
     * @return The created category details.
     */
    CategoryResponse createCategory(Long userId, CategoryRequest request);

    /**
     * Retrieves all categories for a user.
     *
     * @param userId The ID of the user.
     * @return List of categories.
     */
    List<CategoryResponse> getByUser(Long userId);

    /**
     * Retrieves a specific category by ID.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category.
     * @return The category details.
     */
    CategoryResponse getCategoryById(Long userId, Long categoryId);

    /**
     * Retrieves categories of a specific type for a user.
     *
     * @param userId The ID of the user.
     * @param type The type (INCOME/EXPENSE).
     * @return List of categories.
     */
    List<CategoryResponse> getByUserAndType(Long userId, CategoryType type);

    /**
     * Updates an existing category.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category.
     * @param request The updated details.
     * @return The updated category details.
     */
    CategoryResponse updateCategory(Long userId, Long categoryId, CategoryRequest request);

    /**
     * Deletes a category.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category to delete.
     */
    void deleteCategory(Long userId, Long categoryId);

    /**
     * Retrieves all global default categories.
     *
     * @return List of default categories.
     */
    List<CategoryResponse> getDefaultCategories();

    /**
     * Initializes default categories for a new user.
     *
     * @param userId The ID of the user.
     */
    void initDefaultCategories(Long userId);

    /**
     * Sets or updates the budget limit for a category.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category.
     * @param budgetLimit The new budget limit.
     * @return The updated category details.
     */
    CategoryResponse setCategoryBudget(Long userId, Long categoryId, BigDecimal budgetLimit);

    /**
     * Counts the number of categories for a user.
     *
     * @param userId The ID of the user.
     * @return The count of categories.
     */
    long getCategoryCount(Long userId);

    /**
     * Returns a map of category IDs to names for a user.
     *
     * @param userId The ID of the user.
     * @return Map of category ID to name.
     */
    java.util.Map<Long, String> getCategoryNames(Long userId);
}
