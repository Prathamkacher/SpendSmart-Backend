package com.spendsmart.category.resource;

import com.spendsmart.category.constants.AppConstants;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.dto.CategoryResponse;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import com.spendsmart.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing budget categories.
 * Provides endpoints for CRUD operations on user categories and default seeding.
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = AppConstants.SWAGGER_TAG_CATEGORY, description = "CRUD operations for user categories")
public class CategoryResource {

    private final CategoryService categoryService;

    // ── POST /api/categories ────────────────────────────────────────

    /**
     * Creates a new category for the authenticated user.
     *
     * @param request HTTP request containing user context.
     * @param categoryRequest Request body containing category details.
     * @return ApiResponse containing the created category.
     */
    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            HttpServletRequest request,
            @Valid @RequestBody CategoryRequest categoryRequest) {

        Long userId = extractUserId(request);
        CategoryResponse response = categoryService.createCategory(userId, categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.CATEGORY_CREATED, response));
    }

    // ── GET /api/categories ─────────────────────────────────────────

    /**
     * Fetches all categories belonging to the authenticated user.
     *
     * @param request HTTP request containing user context.
     * @return ApiResponse containing a list of categories.
     */
    @GetMapping
    @Operation(summary = "Get all categories for the authenticated user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getUserCategories(
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        List<CategoryResponse> categories = categoryService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CATEGORIES_FETCHED, categories));
    }

    // ── GET /api/categories/{id} ────────────────────────────────────

    /**
     * Fetches a specific category by its ID.
     *
     * @param request HTTP request containing user context.
     * @param id The ID of the category.
     * @return ApiResponse containing the category details.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            HttpServletRequest request,
            @PathVariable Long id) {

        Long userId = extractUserId(request);
        CategoryResponse response = categoryService.getCategoryById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CATEGORY_FETCHED, response));
    }

    // ── GET /api/categories/type ────────────────────────────────────

    /**
     * Fetches categories filtered by type (EXPENSE or INCOME).
     *
     * @param request HTTP request containing user context.
     * @param type The category type to filter by.
     * @return ApiResponse containing filtered categories.
     */
    @GetMapping("/type")
    @Operation(summary = "Get categories filtered by type (EXPENSE or INCOME)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesByType(
            HttpServletRequest request,
            @RequestParam CategoryType type) {

        Long userId = extractUserId(request);
        List<CategoryResponse> categories = categoryService.getByUserAndType(userId, type);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CATEGORIES_FETCHED, categories));
    }

    // ── GET /api/categories/defaults ────────────────────────────────

    /**
     * Fetches all default template categories available in the system.
     *
     * @return ApiResponse containing default categories.
     */
    @GetMapping("/defaults")
    @Operation(summary = "Get all default (template) categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getDefaultCategories() {
        List<CategoryResponse> defaults = categoryService.getDefaultCategories();
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CATEGORIES_FETCHED, defaults));
    }

    // ── PUT /api/categories/{id} ────────────────────────────────────

    /**
     * Updates an existing category for the authenticated user.
     *
     * @param request HTTP request containing user context.
     * @param id The ID of the category to update.
     * @param categoryRequest Updated category details.
     * @return ApiResponse containing updated category.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest) {

        Long userId = extractUserId(request);
        CategoryResponse response = categoryService.updateCategory(userId, id, categoryRequest);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CATEGORY_UPDATED, response));
    }

    // ── PUT /api/categories/{id}/budget ─────────────────────────────

    /**
     * Sets or updates the budget limit for a specific category.
     *
     * @param request HTTP request containing user context.
     * @param id The ID of the category.
     * @param body Map containing "budgetLimit".
     * @return ApiResponse containing the category with updated budget.
     */
    @PutMapping("/{id}/budget")
    @Operation(summary = "Set budget limit for a category")
    public ResponseEntity<ApiResponse<CategoryResponse>> setCategoryBudget(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> body) {

        Long userId = extractUserId(request);
        BigDecimal budgetLimit = body.get("budgetLimit");
        CategoryResponse response = categoryService.setCategoryBudget(userId, id, budgetLimit);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.BUDGET_UPDATED, response));
    }

    // ── DELETE /api/categories/{id} ─────────────────────────────────

    /**
     * Deletes a category belonging to the authenticated user.
     *
     * @param request HTTP request containing user context.
     * @param id The ID of the category to delete.
     * @return ApiResponse indicating success.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            HttpServletRequest request,
            @PathVariable Long id) {

        Long userId = extractUserId(request);
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CATEGORY_DELETED));
    }

    // ── GET /api/categories/count ───────────────────────────────────

    /**
     * Returns the total count of categories for the authenticated user.
     *
     * @param request HTTP request containing user context.
     * @return ApiResponse containing the count.
     */
    @GetMapping("/count")
    @Operation(summary = "Get total category count for user")
    public ResponseEntity<ApiResponse<Long>> getCategoryCount(HttpServletRequest request) {

        Long userId = extractUserId(request);
        long count = categoryService.getCategoryCount(userId);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.COUNT_FETCHED, count));
    }

    // ── POST /api/categories/init ───────────────────────────────────

    /**
     * Manually triggers the seeding of default categories for the authenticated user.
     *
     * @param request HTTP request containing user context.
     * @return ApiResponse indicating success.
     */
    @PostMapping("/init")
    @Operation(summary = "Manually trigger default category seeding for the authenticated user")
    public ResponseEntity<ApiResponse<Void>> initDefaults(HttpServletRequest request) {

        Long userId = extractUserId(request);
        categoryService.initDefaultCategories(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.DEFAULTS_SEEDED));
    }

    /**
     * Returns a map of category IDs to names for the authenticated user.
     *
     * @param request HTTP request containing user context.
     * @return ApiResponse containing the ID-to-Name map.
     */
    @GetMapping("/names")
    @Operation(summary = "Get a map of category IDs to names for the user")
    public ResponseEntity<ApiResponse<Map<Long, String>>> getCategoryNames(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Category names fetched", categoryService.getCategoryNames(userId)));
    }

    // ── Private helpers ─────────────────────────────────────────────

    /**
     * Extracts the user ID from request attributes, handling potential type mismatches.
     *
     * @param request The HTTP request.
     * @return The extracted user ID.
     * @throws UnauthorizedAccessException if user ID is missing.
     */
    private Long extractUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("UserId missing from request attributes in Category Service!");
            throw new UnauthorizedAccessException("User not authenticated");
        }
        
        if (userIdObj instanceof Integer integer) {
            return integer.longValue();
        }
        return (Long) userIdObj;
    }
}
