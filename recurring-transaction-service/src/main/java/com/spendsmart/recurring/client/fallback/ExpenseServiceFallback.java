package com.spendsmart.recurring.client.fallback;

import com.spendsmart.recurring.client.ExpenseServiceClient;
import com.spendsmart.recurring.dto.ExpenseRequest;
import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class ExpenseServiceFallback implements ExpenseServiceClient {

    @Override
    public ApiResponse<Object> createExpense(ExpenseRequest request, String token, Long userId) {
        return ApiResponse.error("Expense Service is currently unavailable. Recurring expense will be retried later.");
    }
}
