package com.spendsmart.category.service.impl;

import com.spendsmart.category.constants.AppConstants;
import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.dto.CategoryResponse;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.exception.CategoryNotFoundException;
import com.spendsmart.category.exception.DuplicateCategoryException;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import com.spendsmart.category.mapper.CategoryMapper;
import com.spendsmart.category.repository.CategoryRepository;
import com.spendsmart.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing budget categories.
 * Handles creation, retrieval, updates, and deletion of categories for users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ── CREATE ───────────────────────────────────────────────────────

    /**
     * Creates a new category for a specific user.
     * Checks for duplicate category names (case-insensitive) for the same user and type.
     *
     * @param userId The ID of the user creating the category.
     * @param request The category details including name, icon, color, and type.
     * @return CategoryResponse containing the created category details.
     * @throws DuplicateCategoryException if a category with the same name and type already exists for the user.
     */
    @Override
    @Transactional
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        log.info("Creating category for userId={}, name='{}'", userId, request.getName());

        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, request.getName(), request.getType())) {
            throw new DuplicateCategoryException(AppConstants.DUPLICATE_CATEGORY);
        }

        Category category = categoryMapper.toEntity(request);
        category.setUserId(userId);
        category.setIsDefault(false);

        if (category.getColorCode() == null) {
            category.setColorCode("#6366F1");
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created: id={}", saved.getCategoryId());
        return categoryMapper.toResponse(saved);
    }

    // ── READ ─────────────────────────────────────────────────────────

    /**
     * Retrieves all categories belonging to a specific user.
     *
     * @param userId The ID of the user whose categories are to be fetched.
     * @return List of CategoryResponse objects ordered by name.
     */
    @Override
    public List<CategoryResponse> getByUser(Long userId) {
        log.debug("Fetching categories for userId={}", userId);
        return categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves a specific category by its ID, ensuring it belongs to the specified user.
     *
     * @param userId The ID of the user requesting the category.
     * @param categoryId The ID of the category to retrieve.
     * @return CategoryResponse details.
     * @throws CategoryNotFoundException if the category does not exist or does not belong to the user.
     */
    @Override
    public CategoryResponse getCategoryById(Long userId, Long categoryId) {
        Category category = findCategoryOrThrow(categoryId, userId);
        return categoryMapper.toResponse(category);
    }

    /**
     * Retrieves categories of a specific type (INCOME/EXPENSE) for a user.
     *
     * @param userId The ID of the user.
     * @param type The type of category to fetch.
     * @return List of CategoryResponse objects of the specified type.
     */
    @Override
    public List<CategoryResponse> getByUserAndType(Long userId, CategoryType type) {
        log.debug("Fetching categories for userId={}, type={}", userId, type);
        return categoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, type)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves all default categories defined in the system.
     *
     * @return List of default CategoryResponse objects.
     */
    @Override
    public List<CategoryResponse> getDefaultCategories() {
        return categoryRepository.findByIsDefaultTrue()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    /**
     * Returns the total count of categories for a specific user.
     *
     * @param userId The ID of the user.
     * @return Total number of categories.
     */
    @Override
    public long getCategoryCount(Long userId) {
        return categoryRepository.countByUserId(userId);
    }

    /**
     * Retrieves a map of category IDs to category names for a specific user.
     * Useful for lookups in other services.
     *
     * @param userId The ID of the user.
     * @return Map containing Category ID as key and Category Name as value.
     */
    @Override
    public java.util.Map<Long, String> getCategoryNames(Long userId) {
        log.debug("Fetching category names for userId={}", userId);
        return categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .filter(c -> c.getCategoryId() != null && c.getName() != null)
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName, (existing, replacement) -> existing));
    }

    // ── UPDATE ───────────────────────────────────────────────────────

    /**
     * Updates an existing category for a user.
     * Validates that the name change does not result in a duplicate.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category to update.
     * @param request The updated category details.
     * @return CategoryResponse with updated details.
     * @throws DuplicateCategoryException if the updated name already exists for the same user and type.
     */
    @Override
    @Transactional
    public CategoryResponse updateCategory(Long userId, Long categoryId, CategoryRequest request) {
        log.info("Updating category id={} for userId={}", categoryId, userId);

        Category existing = findCategoryOrThrow(categoryId, userId);

        // Check for duplicate name if name is changing
        if (!existing.getName().equalsIgnoreCase(request.getName()) &&
            categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, request.getName(), request.getType())) {
            throw new DuplicateCategoryException(AppConstants.DUPLICATE_CATEGORY);
        }

        categoryMapper.updateEntityFromRequest(request, existing);
        Category updated = categoryRepository.save(existing);
        log.info("Category updated: id={}", updated.getCategoryId());
        return categoryMapper.toResponse(updated);
    }

    /**
     * Sets or updates the budget limit for a specific category.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category.
     * @param budgetLimit The new budget limit.
     * @return CategoryResponse with the updated budget limit.
     */
    @Override
    @Transactional
    public CategoryResponse setCategoryBudget(Long userId, Long categoryId, BigDecimal budgetLimit) {
        log.info("Setting budget for category id={}, userId={}, limit={}", categoryId, userId, budgetLimit);

        Category category = findCategoryOrThrow(categoryId, userId);
        category.setBudgetLimit(budgetLimit);
        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────

    /**
     * Deletes a category for a specific user.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category to delete.
     * @throws CategoryNotFoundException if category does not exist or belong to user.
     */
    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        log.info("Deleting category id={} for userId={}", categoryId, userId);
        Category category = findCategoryOrThrow(categoryId, userId);
        categoryRepository.delete(category);
        log.info("Category deleted: id={}", categoryId);
    }

    // ── DEFAULT SEEDING ──────────────────────────────────────────────

    /**
     * Initializes default categories (Food, Transport, Bills, etc.) for a new user.
     * Only seeds categories that the user doesn't already have by name.
     *
     * @param userId The ID of the user to seed categories for.
     */
    @Override
    @Transactional
    public void initDefaultCategories(Long userId) {
        log.info("Checking and seeding missing default categories for user: {}", userId);

        List<String> existingNames = categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(c -> c.getName().toLowerCase())
                .toList();

        // Expense defaults
        seedIfMissing(userId, "Food",          CategoryType.EXPENSE, "\uD83C\uDF54", "#EF4444", existingNames);
        seedIfMissing(userId, "Transport",     CategoryType.EXPENSE, "\uD83D\uDE97", "#F59E0B", existingNames);
        seedIfMissing(userId, "Bills",         CategoryType.EXPENSE, "\uD83D\uDCA1", "#3B82F6", existingNames);
        seedIfMissing(userId, "Health",        CategoryType.EXPENSE, "\uD83C\uDFE5", "#10B981", existingNames);
        seedIfMissing(userId, "Entertainment", CategoryType.EXPENSE, "\uD83C\uDFAC", "#8B5CF6", existingNames);
        seedIfMissing(userId, "Shopping",      CategoryType.EXPENSE, "\uD83D\uDECD", "#EC4899", existingNames);
        seedIfMissing(userId, "Education",     CategoryType.EXPENSE, "\uD83D\uDCDA", "#06B6D4", existingNames);
        seedIfMissing(userId, "Other",         CategoryType.EXPENSE, "\uD83D\uDCE6", "#6B7280", existingNames);

        // Income defaults
        seedIfMissing(userId, "Salary",     CategoryType.INCOME, "\uD83D\uDCBC", "#16A34A", existingNames);
        seedIfMissing(userId, "Freelance",  CategoryType.INCOME, "\uD83D\uDCBB", "#2563EB", existingNames);
        seedIfMissing(userId, "Business",   CategoryType.INCOME, "\uD83C\uDFE2", "#7C3AED", existingNames);
        seedIfMissing(userId, "Investment", CategoryType.INCOME, "\uD83D\uDCC8", "#0891B2", existingNames);
        seedIfMissing(userId, "Gift",       CategoryType.INCOME, "\uD83C\uDF81", "#DB2777", existingNames);

        log.info("Default categories check completed for user: {}", userId);
    }

    /**
     * Helper method to seed a category if it doesn't already exist in the provided list of names.
     */
    private void seedIfMissing(Long userId, String name, CategoryType type, String icon, String colorCode, List<String> existing) {
        if (!existing.contains(name.toLowerCase())) {
            createDefault(userId, name, type, icon, colorCode);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────

    /**
     * Finds a category by ID and user ID or throws a CategoryNotFoundException.
     */
    private Category findCategoryOrThrow(Long categoryId, Long userId) {
        return categoryRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    /**
     * Persists a default category for a user.
     */
    private void createDefault(Long userId, String name, CategoryType type, String icon, String colorCode) {
        Category category = Category.builder()
                .userId(userId)
                .name(name)
                .type(type)
                .icon(icon)
                .colorCode(colorCode)
                .isDefault(true)
                .build();
        categoryRepository.save(category);
    }
}
