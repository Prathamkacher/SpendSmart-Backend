package com.spendsmart.analytics.client;

import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Feign client for communicating with the Budget service.
 * Used to fetch budget constraints for financial health calculations.
 */
@FeignClient(name = "budget-service", path = "/api")
public interface BudgetServiceClient {

    @GetMapping("/budgets/total/user")
    ApiResponse<BigDecimal> getTotalBudget();

    @GetMapping("/budgets/total/month")
    ApiResponse<BigDecimal> getTotalBudgetByMonth(@RequestParam int year, @RequestParam int month);

    @GetMapping("/budgets/active")
    ApiResponse<java.util.List<com.spendsmart.analytics.dto.BudgetDto>> getActiveBudgets();
}
