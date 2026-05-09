package com.spendsmart.recurring.scheduler;

import com.spendsmart.recurring.service.RecurringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringScheduler {

    private final RecurringService recurringService;

    // Run daily at midnight: 0 0 0 * * ?
    @Scheduled(cron = "0 0 0 * * ?")
    public void processDailyRecurringTransactions() {
        log.info("Starting daily recurring transaction processing job");
        try {
            recurringService.processUpcomingDue();
            log.info("Successfully completed daily recurring transaction processing job");
        } catch (Exception e) {
            log.error("Error occurred while processing recurring transactions", e);
        }
    }
}
