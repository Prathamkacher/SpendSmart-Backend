package com.spendsmart.category.repository;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Category} entity.
 * Provides abstraction for database operations on categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds all categories for a specific user, ordered alphabetically by name.
     *
     * @param userId The ID of the user.
     * @return List of categories.
     */
    List<Category> findByUserIdOrderByNameAsc(Long userId);

    /**
     * Finds categories of a specific type for a user, ordered by name.
     *
     * @param userId The ID of the user.
     * @param type The category type (INCOME/EXPENSE).
     * @return List of filtered categories.
     */
    List<Category> findByUserIdAndTypeOrderByNameAsc(Long userId, CategoryType type);

    /**
     * Finds a specific category by its ID and owner ID.
     *
     * @param categoryId The ID of the category.
     * @param userId The ID of the user.
     * @return Optional containing the category if found.
     */
    Optional<Category> findByCategoryIdAndUserId(Long categoryId, Long userId);

    /**
     * Checks if a category with the same name and type already exists for a user.
     *
     * @param userId The ID of the user.
     * @param name The name of the category (case-insensitive).
     * @param type The type of the category.
     * @return true if it exists, false otherwise.
     */
    boolean existsByUserIdAndNameIgnoreCaseAndType(Long userId, String name, CategoryType type);

    /**
     * Retrieves all global default categories.
     *
     * @return List of default categories.
     */
    List<Category> findByIsDefaultTrue();

    /**
     * Counts the total number of categories for a user.
     *
     * @param userId The ID of the user.
     * @return The count of categories.
     */
    long countByUserId(Long userId);

    /**
     * Deletes a category by its ID and user ID.
     *
     * @param categoryId The ID of the category.
     * @param userId The ID of the user.
     */
    void deleteByCategoryIdAndUserId(Long categoryId, Long userId);
}
