package com.spendsmart.recurring.client;

import com.spendsmart.recurring.client.fallback.ExpenseServiceFallback;
import com.spendsmart.recurring.dto.ExpenseRequest;
import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "expense-service", path = "/api", fallback = ExpenseServiceFallback.class)
public interface ExpenseServiceClient {

    @PostMapping("/expenses")
    ApiResponse<Object> createExpense(
            @RequestBody ExpenseRequest request, 
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-User-Id") Long userId);
}
