package com.spendsmart.auth.client;

import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "expense-service", path = "/api/expenses")
public interface ExpenseClient {

    @GetMapping("/admin/all")
    ApiResponse<Page<Map<String, Object>>> getAllExpenses(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("sortDir") String sortDir);

    @GetMapping("/admin/stats")
    ApiResponse<Map<String, Object>> getGlobalStats();
}
