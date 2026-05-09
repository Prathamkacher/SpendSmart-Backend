package com.spendsmart.expense.client;

import com.spendsmart.expense.config.FeignConfig;
import com.spendsmart.expense.dto.BudgetUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Budget-Service communication.
 * Discovered via Eureka using service name "BUDGET-SERVICE".
 * The FeignConfig forwards the JWT Authorization header automatically.
 */
@FeignClient(
        name = "BUDGET-SERVICE",
        configuration = FeignConfig.class,
        fallback = com.spendsmart.expense.client.fallback.BudgetServiceFallback.class
)
public interface BudgetServiceClient {

    /**
     * Update the spent amount for a user's budget category.
     * Positive amount = increment (expense added/increased).
     * Negative amount = decrement (expense deleted/decreased).
     */
    @PutMapping("/api/budgets/spent")
    void updateSpentAmount(@RequestBody BudgetUpdateRequest request);
}
