package com.spendsmart.analytics.scheduler;

import com.spendsmart.analytics.client.AuthServiceClient;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler for automated analytical tasks.
 * Periodically triggers data aggregation and snapshot generation for all users.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsScheduler {

    private final AnalyticsService analyticsService;
    private final AuthServiceClient authServiceClient;

    /**
     * Runs on the 1st day of every month at midnight.
     * Generates snapshots for the previous month.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void generateMonthlySnapshots() {
        log.info("Starting scheduled monthly snapshot generation...");
        
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        ApiResponse<List<Long>> response = authServiceClient.getAllUserIds();
        if (response != null && response.isSuccess() && response.getData() != null) {
            List<Long> userIds = response.getData();
            log.info("Found {} users for snapshot generation", userIds.size());
            
            for (Long userId : userIds) {
                try {
                    analyticsService.generateMonthlySnapshot(userId, year, month);
                } catch (Exception e) {
                    log.error("Failed to generate snapshot for user {}: {}", userId, e.getMessage());
                }
            }
        } else {
            log.warn("Could not fetch user IDs for snapshot generation");
        }
        
        log.info("Scheduled monthly snapshot generation completed.");
    }
}
