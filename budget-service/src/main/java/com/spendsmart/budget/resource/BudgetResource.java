package com.spendsmart.budget.resource;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.BudgetResponse;
import com.spendsmart.budget.dto.BudgetUpdateRequest;
import com.spendsmart.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget Management", description = "Operations for user budgets and tracking")
public class BudgetResource {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Create a new budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            HttpServletRequest request,
            @Valid @RequestBody BudgetRequest budgetRequest) {

        Long userId = extractUserId(request);
        BudgetResponse response = budgetService.createBudget(userId, budgetRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            HttpServletRequest request,
            @PathVariable Long id) {

        Long userId = extractUserId(request);
        BudgetResponse response = budgetService.getBudgetById(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Budget retrieved successfully", response));
    }

    @GetMapping("/user")
    @Operation(summary = "Get all budgets for user")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgetsByUser(
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        List<BudgetResponse> budgets = budgetService.getBudgetsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Budgets retrieved successfully", budgets));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active budgets for user")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getActiveBudgets(
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        List<BudgetResponse> budgets = budgetService.getActiveBudgets(userId);
        return ResponseEntity.ok(ApiResponse.success("Active budgets retrieved successfully", budgets));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest budgetRequest) {

        Long userId = extractUserId(request);
        BudgetResponse response = budgetService.updateBudget(userId, id, budgetRequest);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            HttpServletRequest request,
            @PathVariable Long id) {

        Long userId = extractUserId(request);
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully"));
    }

    @PutMapping("/spent")
    @Operation(summary = "Update spent amount (Inter-service sync)")
    public ResponseEntity<ApiResponse<Void>> updateSpentAmount(
            @RequestBody BudgetUpdateRequest updateRequest) {

        budgetService.updateSpentAmount(updateRequest);
        return ResponseEntity.ok(ApiResponse.success("Spent amount updated successfully"));
    }

    @PostMapping("/reset")
    @Operation(summary = "Manually trigger expired budget reset")
    public ResponseEntity<ApiResponse<Void>> triggerReset() {
        budgetService.resetExpiredBudgets();
        return ResponseEntity.ok(ApiResponse.success("Budget reset triggered successfully"));
    }

    @GetMapping("/total/month")
    @Operation(summary = "Get total budget for month")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getTotalBudgetByMonth(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Monthly total budget fetched", budgetService.getTotalBudgetByMonth(userId, year, month)));
    }

    private Long extractUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("UserId missing from request attributes in Budget Service!");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        if (userIdObj instanceof Integer integer) {
            return integer.longValue();
        }
        return (Long) userIdObj;
    }
}
