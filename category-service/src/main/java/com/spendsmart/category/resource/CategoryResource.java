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

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = AppConstants.SWAGGER_TAG_CATEGORY, description = "CRUD operations for user categories")
public class CategoryResource {

    private final CategoryService categoryService;

    // ── POST /api/categories ────────────────────────────────────────

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

    @GetMapping
    @Operation(summary = "Get all categories for the authenticated user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getUserCategories(
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        List<CategoryResponse> categories = categoryService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CATEGORIES_FETCHED, categories));
    }

    // ── GET /api/categories/{id} ────────────────────────────────────

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

    @GetMapping("/defaults")
    @Operation(summary = "Get all default (template) categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getDefaultCategories() {
        List<CategoryResponse> defaults = categoryService.getDefaultCategories();
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CATEGORIES_FETCHED, defaults));
    }

    // ── PUT /api/categories/{id} ────────────────────────────────────

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

    @GetMapping("/count")
    @Operation(summary = "Get total category count for user")
    public ResponseEntity<ApiResponse<Long>> getCategoryCount(HttpServletRequest request) {

        Long userId = extractUserId(request);
        long count = categoryService.getCategoryCount(userId);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.COUNT_FETCHED, count));
    }

    // ── POST /api/categories/init ───────────────────────────────────

    @PostMapping("/init")
    @Operation(summary = "Manually trigger default category seeding for the authenticated user")
    public ResponseEntity<ApiResponse<Void>> initDefaults(HttpServletRequest request) {

        Long userId = extractUserId(request);
        categoryService.initDefaultCategories(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.DEFAULTS_SEEDED));
    }

    @GetMapping("/names")
    @Operation(summary = "Get a map of category IDs to names for the user")
    public ResponseEntity<ApiResponse<Map<Long, String>>> getCategoryNames(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Category names fetched", categoryService.getCategoryNames(userId)));
    }

    // ── Private helpers ─────────────────────────────────────────────

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
