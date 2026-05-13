package com.spendsmart.income.resource;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.dto.IncomeResponse;
import com.spendsmart.income.entity.IncomeSource;
import com.spendsmart.income.service.IncomeService;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;

/**
 * REST controller for income management.
 * Provides endpoints for recording and tracking user income across different sources.
 */
@Slf4j
@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
@Tag(name = "Income Management", description = "Endpoints for managing user income")
public class IncomeResource {

    private final IncomeService incomeService;

    @PostMapping
    @Operation(summary = "Add a new income")
    public ResponseEntity<ApiResponse<IncomeResponse>> addIncome(
            HttpServletRequest request,
            @Valid @RequestBody IncomeRequest incomeRequest) {
        Long userId = extractUserId(request);
        IncomeResponse response = incomeService.addIncome(userId, incomeRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Income added successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get income by ID")
    public ResponseEntity<ApiResponse<IncomeResponse>> getIncomeById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = extractUserId(request);
        IncomeResponse response = incomeService.getIncomeById(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Income retrieved", response));
    }

    @GetMapping
    @Operation(summary = "Get all incomes for user")
    public ResponseEntity<ApiResponse<Page<IncomeResponse>>> getIncomesByUser(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String[] sort) {
        Long userId = extractUserId(request);
        Pageable pageable = createPageable(page, size, sort);
        Page<IncomeResponse> response = incomeService.getIncomesByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Incomes retrieved", response));
    }

    @GetMapping("/source")
    @Operation(summary = "Get incomes by source")
    public ResponseEntity<ApiResponse<Page<IncomeResponse>>> getIncomesBySource(
            HttpServletRequest request,
            @RequestParam IncomeSource source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String[] sort) {
        Long userId = extractUserId(request);
        Pageable pageable = createPageable(page, size, sort);
        Page<IncomeResponse> response = incomeService.getIncomesBySource(userId, source, pageable);
        return ResponseEntity.ok(ApiResponse.success("Incomes retrieved for source: " + source, response));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get incomes by date range")
    public ResponseEntity<ApiResponse<Page<IncomeResponse>>> getIncomesByDateRange(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String[] sort) {
        Long userId = extractUserId(request);
        Pageable pageable = createPageable(page, size, sort);
        Page<IncomeResponse> response = incomeService.getIncomesByDateRange(userId, start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success("Incomes retrieved for date range", response));
    }

    @GetMapping("/month")
    @Operation(summary = "Get incomes by month")
    public ResponseEntity<ApiResponse<Page<IncomeResponse>>> getIncomesByMonth(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String[] sort) {
        Long userId = extractUserId(request);
        Pageable pageable = createPageable(page, size, sort);
        Page<IncomeResponse> response = incomeService.getIncomesByMonth(userId, year, month, pageable);
        return ResponseEntity.ok(ApiResponse.success("Incomes retrieved for month: " + month + "/" + year, response));
    }

    @GetMapping("/recurring")
    @Operation(summary = "Get all recurring incomes")
    public ResponseEntity<ApiResponse<List<IncomeResponse>>> getRecurringIncomes() {
        List<IncomeResponse> response = incomeService.getRecurringIncomes();
        return ResponseEntity.ok(ApiResponse.success("Recurring incomes retrieved", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing income")
    public ResponseEntity<ApiResponse<IncomeResponse>> updateIncome(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody IncomeRequest incomeRequest) {
        Long userId = extractUserId(request);
        IncomeResponse response = incomeService.updateIncome(userId, id, incomeRequest);
        return ResponseEntity.ok(ApiResponse.success("Income updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an income")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = extractUserId(request);
        incomeService.deleteIncome(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Income deleted successfully", null));
    }

    @GetMapping("/total/user")
    @Operation(summary = "Get total income for user")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalIncomeByUser(HttpServletRequest request) {
        Long userId = extractUserId(request);
        BigDecimal total = incomeService.getTotalIncomeByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Total income calculated", total));
    }

    @GetMapping("/total/month")
    @Operation(summary = "Get total income for month")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalIncomeByMonth(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = extractUserId(request);
        BigDecimal total = incomeService.getTotalIncomeByMonth(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success("Monthly total calculated", total));
    }

    // ── ADMIN ENDPOINTS ──────────────────────────────────────────────

    @GetMapping("/admin/all")
    @Operation(summary = "Get all platform incomes (Internal/Admin only)")
    public ResponseEntity<ApiResponse<Page<IncomeResponse>>> getAllIncomesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "date,desc") String[] sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        Page<IncomeResponse> incomes = incomeService.getAllIncomes(pageable);
        return ResponseEntity.ok(ApiResponse.success("All platform incomes fetched", incomes));
    }

    @GetMapping("/admin/stats")
    @Operation(summary = "Get global platform income stats (Internal/Admin only)")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getGlobalStats() {
        java.util.Map<String, Object> stats = java.util.Map.of(
            "totalAmount", incomeService.getGlobalTotalIncome(),
            "totalCount", incomeService.getGlobalIncomeCount()
        );
        return ResponseEntity.ok(ApiResponse.success("Global income stats fetched", stats));
    }

    private Long extractUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("UserId missing from request attributes in Income Service!");
            throw new UnauthorizedAccessException("User not authenticated or userId missing");
        }
        
        if (userIdObj instanceof Integer integer) {
            return integer.longValue();
        }
        return (Long) userIdObj;
    }

    private Pageable createPageable(int page, int size, String[] sort) {
        Sort sorting = Sort.by(sort[0]);
        if (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) {
            sorting = sorting.descending();
        }
        return PageRequest.of(page, size, sorting);
    }
}
