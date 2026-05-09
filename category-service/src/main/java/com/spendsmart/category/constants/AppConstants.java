package com.spendsmart.category.constants;

public final class AppConstants {

    private AppConstants() {}

    // --- JWT ---
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTH_HEADER   = "Authorization";
    public static final String ROLE_USER     = "ROLE_USER";

    // --- Success messages ---
    public static final String CATEGORY_CREATED     = "Category created successfully";
    public static final String CATEGORY_UPDATED     = "Category updated successfully";
    public static final String CATEGORY_DELETED     = "Category deleted successfully";
    public static final String CATEGORY_FETCHED     = "Category retrieved successfully";
    public static final String CATEGORIES_FETCHED   = "Categories retrieved successfully";
    public static final String DEFAULTS_SEEDED      = "Default categories initialized successfully";
    public static final String BUDGET_UPDATED       = "Budget limit updated successfully";
    public static final String COUNT_FETCHED        = "Category count retrieved successfully";

    // --- Error messages ---
    public static final String CATEGORY_NOT_FOUND   = "Category not found with id: ";
    public static final String UNAUTHORIZED_ACCESS  = "You are not authorized to access this resource";
    public static final String DUPLICATE_CATEGORY   = "A category with this name already exists for the given type";

    // --- Swagger tags ---
    public static final String SWAGGER_TAG_CATEGORY = "Category Management";
}
