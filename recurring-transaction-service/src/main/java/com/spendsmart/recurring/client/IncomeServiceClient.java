package com.spendsmart.recurring.client;

import com.spendsmart.recurring.client.fallback.IncomeServiceFallback;
import com.spendsmart.recurring.dto.IncomeRequest;
import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "income-service", path = "/api", fallback = IncomeServiceFallback.class)
public interface IncomeServiceClient {

    @PostMapping("/incomes")
    ApiResponse<Object> createIncome(
            @RequestBody IncomeRequest request, 
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-User-Id") Long userId);
}
