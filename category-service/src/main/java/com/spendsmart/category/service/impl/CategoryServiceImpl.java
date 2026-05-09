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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ── CREATE ───────────────────────────────────────────────────────

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

    @Override
    public List<CategoryResponse> getByUser(Long userId) {
        log.debug("Fetching categories for userId={}", userId);
        return categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long userId, Long categoryId) {
        Category category = findCategoryOrThrow(categoryId, userId);
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getByUserAndType(Long userId, CategoryType type) {
        log.debug("Fetching categories for userId={}, type={}", userId, type);
        return categoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, type)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getDefaultCategories() {
        return categoryRepository.findByIsDefaultTrue()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public long getCategoryCount(Long userId) {
        return categoryRepository.countByUserId(userId);
    }

    @Override
    public java.util.Map<Long, String> getCategoryNames(Long userId) {
        log.debug("Fetching category names for userId={}", userId);
        return categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .filter(c -> c.getCategoryId() != null && c.getName() != null)
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName, (existing, replacement) -> existing));
    }

    // ── UPDATE ───────────────────────────────────────────────────────

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

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        log.info("Deleting category id={} for userId={}", categoryId, userId);
        Category category = findCategoryOrThrow(categoryId, userId);
        categoryRepository.delete(category);
        log.info("Category deleted: id={}", categoryId);
    }

    // ── DEFAULT SEEDING ──────────────────────────────────────────────

    @Override
    @Transactional
    public void initDefaultCategories(Long userId) {
        log.info("Checking and seeding missing default categories for user: {}", userId);

        List<String> existingNames = categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(c -> c.getName().toLowerCase())
                .toList();

        // Expense defaults
        seedIfMissing(userId, "Food",          CategoryType.EXPENSE, "🍔", "#EF4444", existingNames);
        seedIfMissing(userId, "Transport",     CategoryType.EXPENSE, "🚗", "#F59E0B", existingNames);
        seedIfMissing(userId, "Bills",         CategoryType.EXPENSE, "💡", "#3B82F6", existingNames);
        seedIfMissing(userId, "Health",        CategoryType.EXPENSE, "🏥", "#10B981", existingNames);
        seedIfMissing(userId, "Entertainment", CategoryType.EXPENSE, "🎬", "#8B5CF6", existingNames);
        seedIfMissing(userId, "Shopping",      CategoryType.EXPENSE, "🛍️", "#EC4899", existingNames);
        seedIfMissing(userId, "Education",     CategoryType.EXPENSE, "📚", "#06B6D4", existingNames);
        seedIfMissing(userId, "Other",         CategoryType.EXPENSE, "📦", "#6B7280", existingNames);

        // Income defaults
        seedIfMissing(userId, "Salary",     CategoryType.INCOME, "💼", "#16A34A", existingNames);
        seedIfMissing(userId, "Freelance",  CategoryType.INCOME, "💻", "#2563EB", existingNames);
        seedIfMissing(userId, "Business",   CategoryType.INCOME, "🏢", "#7C3AED", existingNames);
        seedIfMissing(userId, "Investment", CategoryType.INCOME, "📈", "#0891B2", existingNames);
        seedIfMissing(userId, "Gift",       CategoryType.INCOME, "🎁", "#DB2777", existingNames);

        log.info("Default categories check completed for user: {}", userId);
    }

    private void seedIfMissing(Long userId, String name, CategoryType type, String icon, String colorCode, List<String> existing) {
        if (!existing.contains(name.toLowerCase())) {
            createDefault(userId, name, type, icon, colorCode);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────

    private Category findCategoryOrThrow(Long categoryId, Long userId) {
        return categoryRepository.findByCategoryIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

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
