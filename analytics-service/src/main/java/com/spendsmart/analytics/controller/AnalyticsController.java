package com.spendsmart.analytics.controller;

import com.spendsmart.analytics.dto.*;
import com.spendsmart.analytics.service.AnalyticsService;
import com.spendsmart.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.spendsmart.analytics.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for financial analytics and reporting.
 * Provides endpoints for monthly/yearly summaries, category breakdowns, trends, and reports.
 */
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Financial insights and reporting")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ReportService reportService;

    @GetMapping("/monthlySummary")
    @Operation(summary = "Get monthly financial summary")
    public ResponseEntity<ApiResponse<MonthlySummary>> getMonthlySummary(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        
        return ResponseEntity.ok(ApiResponse.success("Monthly summary fetched", analyticsService.getMonthlySummary(userId, y, m)));
    }

    @GetMapping("/yearlySummary")
    @Operation(summary = "Get yearly financial summary")
    public ResponseEntity<ApiResponse<List<MonthlySummary>>> getYearlySummary(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        
        return ResponseEntity.ok(ApiResponse.success("Yearly summary fetched", analyticsService.getYearlySummary(userId, y)));
    }

    @GetMapping("/categoryBreakdown")
    @Operation(summary = "Get expense breakdown by category")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getCategoryBreakdown(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        
        return ResponseEntity.ok(ApiResponse.success("Category breakdown fetched", analyticsService.getExpenseBreakdownByCategory(userId, y, m)));
    }

    @GetMapping("/incomeVsExpense")
    @Operation(summary = "Get income vs expense trend")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Double>>>> getIncomeVsExpenseTrend(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        
        return ResponseEntity.ok(ApiResponse.success("Income vs Expense trend fetched", analyticsService.getIncomeVsExpenseTrend(userId, y)));
    }

    @GetMapping("/savingsRate")
    @Operation(summary = "Get savings rate trend")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getSavingsRateTrend(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        
        return ResponseEntity.ok(ApiResponse.success("Savings rate trend fetched", analyticsService.getSavingsRateTrend(userId, y)));
    }

    @GetMapping("/topCategories")
    @Operation(summary = "Get top spending categories")
    public ResponseEntity<ApiResponse<List<TopCategory>>> getTopCategories(
            HttpServletRequest request,
            @RequestParam(defaultValue = "5") int limit) {
        
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Top categories fetched", analyticsService.getTopSpendingCategories(userId, limit)));
    }

    @GetMapping("/dailyTrend")
    @Operation(summary = "Get daily expense trend")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getDailyTrend(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        
        return ResponseEntity.ok(ApiResponse.success("Daily trend fetched", analyticsService.getDailyExpenseTrend(userId, y, m)));
    }

    @GetMapping("/cashflow")
    @Operation(summary = "Get cashflow data")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getCashflow(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        
        return ResponseEntity.ok(ApiResponse.success("Cashflow data fetched", analyticsService.getCashflowData(userId, y, m)));
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get spending forecast")
    public ResponseEntity<ApiResponse<ForecastResponse>> getForecast(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Forecast fetched", analyticsService.getSpendingForecast(userId)));
    }

    @GetMapping("/healthScore")
    @Operation(summary = "Get financial health score")
    public ResponseEntity<ApiResponse<HealthScoreResponse>> getHealthScore(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        
        return ResponseEntity.ok(ApiResponse.success("Health score fetched", analyticsService.getFinancialHealthScore(userId, y, m)));
    }

    @GetMapping("/reports/download")
    @Operation(summary = "Download monthly financial report (PRO users)")
    public ResponseEntity<byte[]> downloadMonthlyReport(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        Long userId = extractUserId(request);
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        
        String csvData = reportService.generateMonthlyReportCsv(userId, y, m);
        byte[] output = csvData.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", String.format("spendsmart-report-%d-%02d.csv", y, m));

        return ResponseEntity.ok()
                .headers(headers)
                .body(output);
    }

    private Long extractUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("UserId missing from request attributes in Analytics Service!");
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        if (userIdObj instanceof Integer integer) {
            return integer.longValue();
        }
        return (Long) userIdObj;
    }
}
