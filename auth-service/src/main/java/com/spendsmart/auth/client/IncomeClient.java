package com.spendsmart.auth.client;

import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "income-service", path = "/api/incomes")
public interface IncomeClient {

    @GetMapping("/admin/all")
    ApiResponse<Page<Map<String, Object>>> getAllIncomes(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String[] sort);

    @GetMapping("/admin/stats")
    ApiResponse<Map<String, Object>> getGlobalStats();
}
