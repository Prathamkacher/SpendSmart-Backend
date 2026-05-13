package com.spendsmart.analytics.client;

import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign client for communicating with the Expense service.
 * Used to fetch expense data for spending analysis and trends.
 */
@FeignClient(name = "expense-service", path = "/api")
public interface ExpenseServiceClient {

    @GetMapping("/expenses/total/user")
    ApiResponse<BigDecimal> getTotalExpenses();

    @GetMapping("/expenses/total/month")
    ApiResponse<BigDecimal> getTotalExpensesByMonth(@RequestParam int year, @RequestParam int month);

    @GetMapping("/expenses/category-breakdown")
    ApiResponse<Map<String, BigDecimal>> getCategoryBreakdown(@RequestParam int year, @RequestParam int month);
    
    @GetMapping("/expenses/daily-trend")
    ApiResponse<Map<String, BigDecimal>> getDailyTrend(@RequestParam int year, @RequestParam int month);
}
