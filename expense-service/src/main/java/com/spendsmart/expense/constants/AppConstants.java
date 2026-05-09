package com.spendsmart.expense.constants;

/**
 * Central place for all string/numeric constants.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class — prevent instantiation
    }

    // --- JWT ---
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTH_HEADER   = "Authorization";
    public static final String ROLE_USER     = "ROLE_USER";

    // --- Success messages ---
    public static final String EXPENSE_CREATED     = "Expense created successfully";
    public static final String EXPENSE_UPDATED     = "Expense updated successfully";
    public static final String EXPENSE_DELETED     = "Expense deleted successfully";
    public static final String EXPENSE_FETCHED     = "Expense retrieved successfully";
    public static final String EXPENSES_FETCHED    = "Expenses retrieved successfully";
    public static final String TOTAL_FETCHED       = "Total amount retrieved successfully";

    // --- Error messages ---
    public static final String EXPENSE_NOT_FOUND   = "Expense not found with id: ";
    public static final String UNAUTHORIZED_ACCESS = "You are not authorized to access this resource";
    public static final String BUDGET_UPDATE_FAILED = "Failed to update budget service. Expense operation completed but budget may be inconsistent.";

    // --- Swagger tags ---
    public static final String SWAGGER_TAG_EXPENSE = "Expense Management";

    // --- Defaults ---
    public static final String DEFAULT_CURRENCY    = "INR";
    public static final int    DEFAULT_PAGE_SIZE   = 20;
    public static final String DEFAULT_SORT_FIELD  = "date";
    public static final String DEFAULT_SORT_DIR    = "desc";
    public static final String UNKNOWN_CATEGORY   = "Unknown Category";
}
