package com.spendsmart.budget.scheduler;

import com.spendsmart.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetResetScheduler {

    private final BudgetService budgetService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void resetBudgets() {
        log.info("Starting scheduled budget reset task...");
        try {
            budgetService.resetExpiredBudgets();
            log.info("Scheduled budget reset task completed successfully.");
        } catch (Exception e) {
            log.error("Error occurred during scheduled budget reset task: {}", e.getMessage(), e);
        }
    }
}
