package com.spendsmart.recurring.client.fallback;

import com.spendsmart.recurring.client.IncomeServiceClient;
import com.spendsmart.recurring.dto.IncomeRequest;
import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class IncomeServiceFallback implements IncomeServiceClient {

    @Override
    public ApiResponse<Object> createIncome(IncomeRequest request, String token, Long userId) {
        return ApiResponse.error("Income Service is currently unavailable. Recurring income will be retried later.");
    }
}
