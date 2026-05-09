package com.spendsmart.analytics.controller;

import com.spendsmart.analytics.dto.*;
import com.spendsmart.analytics.service.AnalyticsService;
import com.spendsmart.analytics.service.ReportService;
import com.spendsmart.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsController Unit Tests")
class AnalyticsControllerTest {

    @Mock private AnalyticsService analyticsService;
    @Mock private ReportService reportService;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private AnalyticsController analyticsController;

    private void mockUserId() {
        when(httpRequest.getAttribute("userId")).thenReturn(1L);
    }

    @Test
    @DisplayName("getMonthlySummary() - should return OK")
    void getMonthlySummary_ShouldReturnOk() {
        mockUserId();
        MonthlySummary summary = MonthlySummary.builder().totalIncome(new BigDecimal("5000")).build();
        when(analyticsService.getMonthlySummary(eq(1L), anyInt(), anyInt())).thenReturn(summary);

        ResponseEntity<ApiResponse<MonthlySummary>> response = analyticsController.getMonthlySummary(httpRequest, 2026, 4);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getMonthlySummary() - null params should use defaults")
    void getMonthlySummary_NullParams_ShouldUseDefaults() {
        mockUserId();
        MonthlySummary summary = MonthlySummary.builder().build();
        when(analyticsService.getMonthlySummary(eq(1L), anyInt(), anyInt())).thenReturn(summary);

        ResponseEntity<ApiResponse<MonthlySummary>> response = analyticsController.getMonthlySummary(httpRequest, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getYearlySummary() - should return OK")
    void getYearlySummary_ShouldReturnOk() {
        mockUserId();
        when(analyticsService.getYearlySummary(eq(1L), anyInt())).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<List<MonthlySummary>>> response = analyticsController.getYearlySummary(httpRequest, 2026);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getCategoryBreakdown() - should return OK")
    void getCategoryBreakdown_ShouldReturnOk() {
        mockUserId();
        when(analyticsService.getExpenseBreakdownByCategory(eq(1L), anyInt(), anyInt())).thenReturn(Map.of("Food", 500.0));

        ResponseEntity<ApiResponse<Map<String, Double>>> response = analyticsController.getCategoryBreakdown(httpRequest, 2026, 4);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getIncomeVsExpenseTrend() - should return OK")
    void getIncomeVsExpenseTrend_ShouldReturnOk() {
        mockUserId();
        when(analyticsService.getIncomeVsExpenseTrend(eq(1L), anyInt())).thenReturn(Collections.emptyMap());

        ResponseEntity<?> response = analyticsController.getIncomeVsExpenseTrend(httpRequest, 2026);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getSavingsRateTrend() - should return OK")
    void getSavingsRateTrend_ShouldReturnOk() {
        mockUserId();
        when(analyticsService.getSavingsRateTrend(eq(1L), anyInt())).thenReturn(Collections.emptyMap());

        ResponseEntity<?> response = analyticsController.getSavingsRateTrend(httpRequest, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getTopCategories() - should return OK")
    void getTopCategories_ShouldReturnOk() {
        mockUserId();
        when(analyticsService.getTopSpendingCategories(eq(1L), anyInt())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = analyticsController.getTopCategories(httpRequest, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getDailyTrend() - should return OK")
    void getDailyTrend_ShouldReturnOk() {
        mockUserId();
        when(analyticsService.getDailyExpenseTrend(eq(1L), anyInt(), anyInt())).thenReturn(Collections.emptyMap());

        ResponseEntity<?> response = analyticsController.getDailyTrend(httpRequest, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getCashflow() - should return OK")
    void getCashflow_ShouldReturnOk() {
        mockUserId();
        when(analyticsService.getCashflowData(eq(1L), anyInt(), anyInt())).thenReturn(Collections.emptyMap());

        ResponseEntity<?> response = analyticsController.getCashflow(httpRequest, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getForecast() - should return OK")
    void getForecast_ShouldReturnOk() {
        mockUserId();
        ForecastResponse forecast = ForecastResponse.builder().confidence("HIGH").build();
        when(analyticsService.getSpendingForecast(1L)).thenReturn(forecast);

        ResponseEntity<ApiResponse<ForecastResponse>> response = analyticsController.getForecast(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getHealthScore() - should return OK")
    void getHealthScore_ShouldReturnOk() {
        mockUserId();
        HealthScoreResponse score = HealthScoreResponse.builder().score(85).status("EXCELLENT").build();
        when(analyticsService.getFinancialHealthScore(eq(1L), anyInt(), anyInt())).thenReturn(score);

        ResponseEntity<ApiResponse<HealthScoreResponse>> response = analyticsController.getHealthScore(httpRequest, 2026, 4);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("downloadMonthlyReport() - should return CSV")
    void downloadMonthlyReport_ShouldReturnCsv() {
        mockUserId();
        when(reportService.generateMonthlyReportCsv(eq(1L), anyInt(), anyInt())).thenReturn("Date,Amount\n2026-04-01,500");

        ResponseEntity<byte[]> response = analyticsController.downloadMonthlyReport(httpRequest, 2026, 4);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new String(response.getBody())).contains("Date,Amount");
    }

    @Test
    @DisplayName("extractUserId() - should throw when null")
    void extractUserId_Null_ShouldThrow() {
        when(httpRequest.getAttribute("userId")).thenReturn(null);
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> analyticsController.getForecast(httpRequest));
    }

    @Test
    @DisplayName("extractUserId() - should handle Integer")
    void extractUserId_Integer_ShouldConvert() {
        when(httpRequest.getAttribute("userId")).thenReturn(Integer.valueOf(1));
        ForecastResponse forecast = ForecastResponse.builder().build();
        when(analyticsService.getSpendingForecast(1L)).thenReturn(forecast);

        ResponseEntity<ApiResponse<ForecastResponse>> response = analyticsController.getForecast(httpRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
