package com.spendsmart.analytics.client;

import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Feign client for communicating with the Income service.
 * Used to fetch income data for cash flow analysis.
 */
@FeignClient(name = "income-service", path = "/api")
public interface IncomeServiceClient {

    @GetMapping("/incomes/total/user")
    ApiResponse<BigDecimal> getTotalIncome();

    @GetMapping("/incomes/total/month")
    ApiResponse<BigDecimal> getTotalIncomeByMonth(@RequestParam int year, @RequestParam int month);
}
