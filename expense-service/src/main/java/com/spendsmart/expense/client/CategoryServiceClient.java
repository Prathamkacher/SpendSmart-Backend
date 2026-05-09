package com.spendsmart.expense.client;

import com.spendsmart.expense.client.fallback.CategoryServiceFallback;
import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "category-service", path = "/api", fallback = CategoryServiceFallback.class)
public interface CategoryServiceClient {

    @GetMapping("/categories/names")
    ApiResponse<Map<Long, String>> getCategoryNames();
    
    @GetMapping("/categories/{id}")
    ApiResponse<Map<String, Object>> getCategoryById(@PathVariable Long id);
}
