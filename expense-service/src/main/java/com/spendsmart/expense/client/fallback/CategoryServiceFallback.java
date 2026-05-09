package com.spendsmart.expense.client.fallback;

import com.spendsmart.expense.client.CategoryServiceClient;
import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class CategoryServiceFallback implements CategoryServiceClient {

    @Override
    public ApiResponse<Map<Long, String>> getCategoryNames() {
        return ApiResponse.<Map<Long, String>>builder()
                .success(false)
                .message("Category service is down. Unable to fetch category names.")
                .data(Collections.emptyMap())
                .build();
    }

    @Override
    public ApiResponse<Map<String, Object>> getCategoryById(Long id) {
        return ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("Category service is down. Unable to fetch category details.")
                .data(Collections.emptyMap())
                .build();
    }
}
