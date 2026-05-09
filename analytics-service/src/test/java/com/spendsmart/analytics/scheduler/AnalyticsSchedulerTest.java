package com.spendsmart.analytics.scheduler;

import com.spendsmart.analytics.client.AuthServiceClient;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.analytics.service.AnalyticsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyticsSchedulerTest {

    @Test
    void generateMonthlySnapshots_ShouldProcessEveryReturnedUserEvenIfOneFails() {
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        AuthServiceClient authServiceClient = mock(AuthServiceClient.class);
        AnalyticsScheduler scheduler = new AnalyticsScheduler(analyticsService, authServiceClient);

        when(authServiceClient.getAllUserIds()).thenReturn(ApiResponse.success("ok", List.of(10L, 20L)));
        doThrow(new IllegalStateException("boom")).when(analyticsService)
                .generateMonthlySnapshot(org.mockito.ArgumentMatchers.eq(20L), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());

        scheduler.generateMonthlySnapshots();

        verify(analyticsService).generateMonthlySnapshot(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
        verify(analyticsService).generateMonthlySnapshot(org.mockito.ArgumentMatchers.eq(20L), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void generateMonthlySnapshots_ShouldSkipProcessingWhenUserResponseIsInvalid() {
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        AuthServiceClient authServiceClient = mock(AuthServiceClient.class);
        AnalyticsScheduler scheduler = new AnalyticsScheduler(analyticsService, authServiceClient);

        when(authServiceClient.getAllUserIds()).thenReturn(ApiResponse.<List<Long>>error("down"));

        scheduler.generateMonthlySnapshots();

        verify(analyticsService, never()).generateMonthlySnapshot(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void generateMonthlySnapshots_ShouldSkipProcessingWhenNoResponseExists() {
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        AuthServiceClient authServiceClient = mock(AuthServiceClient.class);
        AnalyticsScheduler scheduler = new AnalyticsScheduler(analyticsService, authServiceClient);

        when(authServiceClient.getAllUserIds()).thenReturn(null);

        scheduler.generateMonthlySnapshots();

        verify(analyticsService, times(0)).generateMonthlySnapshot(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }
}
