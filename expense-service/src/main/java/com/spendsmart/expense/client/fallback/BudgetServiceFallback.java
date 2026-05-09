package com.spendsmart.expense.client.fallback;

import com.spendsmart.expense.client.BudgetServiceClient;
import com.spendsmart.expense.dto.BudgetUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BudgetServiceFallback implements BudgetServiceClient {

    @Override
    public void updateSpentAmount(BudgetUpdateRequest request) {
        log.error("Failed to update budget for user {} category {}. Budget Service is down.", 
                request.getUserId(), request.getCategoryId());
        // In a real scenario, we might use a retry mechanism or a dead-letter queue.
    }
}
