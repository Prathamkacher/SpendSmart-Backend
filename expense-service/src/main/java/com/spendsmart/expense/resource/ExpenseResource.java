package com.spendsmart.expense.resource;

import com.spendsmart.expense.constants.AppConstants;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.dto.ExpenseResponse;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.service.ExpenseService;
import com.spendsmart.expense.exception.ExpenseNotFoundException;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * REST controller for expense management.
 * Provides endpoints for recording, searching, and aggregating expense data.
 */
@Slf4j
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Tag(name = AppConstants.SWAGGER_TAG_EXPENSE, description = "Create, Read, Update, Delete expenses")
public class ExpenseResource {

    private final ExpenseService expenseService;

    // ── POST /api/expenses ──────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> addExpense(
            HttpServletRequest request,
            @Valid @RequestBody ExpenseRequest expenseRequest) {

        Long userId = extractUserId(request);
        ExpenseResponse response = expenseService.addExpense(userId, expenseRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.EXPENSE_CREATED, response));
    }

    // ── GET /api/expenses ──────────────────────────────────────────
    
    @GetMapping
    @Operation(summary = "Get all expenses for the authenticated user (paginated)")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getExpenses(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long userId = extractUserId(request);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSES_FETCHED, expenses));
    }

    // ── GET /api/expenses/{id} ──────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            HttpServletRequest request,
            @PathVariable Long id) {

        Long userId = extractUserId(request);
        ExpenseResponse response = expenseService.getExpenseById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSE_FETCHED, response));
    }

    // ── GET /api/expenses/user/{userId} ─────────────────────────────

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all expenses for a user (paginated)")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getExpensesByUser(
            HttpServletRequest request,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long authUserId = extractUserId(request);
        verifyUserAccess(authUserId, userId);

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSES_FETCHED, expenses));
    }

    // ── GET /api/expenses/category/{categoryId} ─────────────────────

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get expenses by category (paginated)")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getExpensesByCategory(
            HttpServletRequest request,
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long userId = extractUserId(request);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByCategory(userId, categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSES_FETCHED, expenses));
    }

    // ── GET /api/expenses/date-range ────────────────────────────────

    @GetMapping("/date-range")
    @Operation(summary = "Get expenses by date range (paginated)")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getExpensesByDateRange(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long userId = extractUserId(request);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByDateRange(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSES_FETCHED, expenses));
    }

    // ── GET /api/expenses/month ─────────────────────────────────────

    @GetMapping("/month")
    @Operation(summary = "Get expenses by month and year (paginated)")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getExpensesByMonth(
            HttpServletRequest request,
            @RequestParam @Parameter(description = "Year (e.g. 2026)") int year,
            @RequestParam @Parameter(description = "Month (1-12)") int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long userId = extractUserId(request);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByMonth(userId, year, month, pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSES_FETCHED, expenses));
    }

    // ── GET /api/expenses/type ──────────────────────────────────────

    @GetMapping("/type")
    @Operation(summary = "Get expenses by type (paginated)")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getExpensesByType(
            HttpServletRequest request,
            @RequestParam ExpenseType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long userId = extractUserId(request);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<ExpenseResponse> expenses = expenseService.getExpensesByType(userId, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSES_FETCHED, expenses));
    }

    // ── GET /api/expenses/search ────────────────────────────────────

    @GetMapping("/search")
    @Operation(summary = "Search expenses by keyword in title and notes")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> searchExpenses(
            HttpServletRequest request,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long userId = extractUserId(request);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<ExpenseResponse> expenses = expenseService.searchExpenses(userId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSES_FETCHED, expenses));
    }

    // ── PUT /api/expenses/{id} ──────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest expenseRequest) {

        Long userId = extractUserId(request);
        ExpenseResponse response = expenseService.updateExpense(userId, id, expenseRequest);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSE_UPDATED, response));
    }

    // ── DELETE /api/expenses/{id} ───────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            HttpServletRequest request,
            @PathVariable Long id) {

        Long userId = extractUserId(request);
        expenseService.deleteExpense(userId, id);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.EXPENSE_DELETED));
    }

    // ── GET /api/expenses/total/user ────────────────────────────────

    @GetMapping("/total/user")
    @Operation(summary = "Get total expense amount for the authenticated user")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalByUser(HttpServletRequest request) {

        Long userId = extractUserId(request);
        BigDecimal total = expenseService.getTotalByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.TOTAL_FETCHED, total));
    }

    // ── GET /api/expenses/total/category ────────────────────────────

    @GetMapping("/total/category")
    @Operation(summary = "Get total expense amount for a specific category")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalByCategory(
            HttpServletRequest request,
            @RequestParam Long categoryId) {

        Long userId = extractUserId(request);
        BigDecimal total = expenseService.getTotalByCategory(userId, categoryId);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.TOTAL_FETCHED, total));
    }

    @GetMapping("/total/month")
    @Operation(summary = "Get total expense amount for a specific month")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalByMonth(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = extractUserId(request);
        BigDecimal total = expenseService.getTotalByMonth(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.TOTAL_FETCHED, total));
    }

    @GetMapping("/category-breakdown")
    @Operation(summary = "Get expense breakdown by category")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getCategoryBreakdown(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = extractUserId(request);
        Map<String, BigDecimal> breakdown = expenseService.getCategoryBreakdown(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success("Category breakdown fetched", breakdown));
    }

    @GetMapping("/daily-trend")
    @Operation(summary = "Get daily expense trend for a specific month")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getDailyTrend(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = extractUserId(request);
        Map<String, BigDecimal> trend = expenseService.getDailyTrend(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success("Daily trend fetched", trend));
    }

    // ── ADMIN ENDPOINTS ──────────────────────────────────────────────

    @GetMapping("/admin/all")
    @Operation(summary = "Get all platform expenses (Internal/Admin only)")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getAllExpensesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<ExpenseResponse> expenses = expenseService.getAllExpenses(pageable);
        return ResponseEntity.ok(ApiResponse.success("All platform expenses fetched", expenses));
    }

    @GetMapping("/admin/stats")
    @Operation(summary = "Get global platform expense stats (Internal/Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGlobalStats() {
        Map<String, Object> stats = Map.of(
            "totalAmount", expenseService.getGlobalTotalExpenses(),
            "totalCount", expenseService.getGlobalExpenseCount()
        );
        return ResponseEntity.ok(ApiResponse.success("Global expense stats fetched", stats));
    }

    // ── Private helpers ─────────────────────────────────────────────

    /**
     * Extract userId from the JWT-authenticated request.
     */
    private Long extractUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("UserId attribute missing in request! Check JwtAuthenticationFilter.");
            throw new UnauthorizedAccessException("User identification failed");
        }
        
        if (userIdObj instanceof Integer integer) {
            return integer.longValue();
        }
        return (Long) userIdObj;
    }

    /**
     * Verify the authenticated user is accessing their own data.
     */
    private void verifyUserAccess(Long authUserId, Long requestedUserId) {
        if (!authUserId.equals(requestedUserId)) {
            throw new UnauthorizedAccessException(
                    AppConstants.UNAUTHORIZED_ACCESS);
        }
    }

    /**
     * Build a Pageable with sorting support.
     */
    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
