package com.spendsmart.analytics.service.impl;

import com.spendsmart.analytics.client.BudgetServiceClient;
import com.spendsmart.analytics.client.ExpenseServiceClient;
import com.spendsmart.analytics.client.IncomeServiceClient;
import com.spendsmart.analytics.dto.BudgetDto;
import com.spendsmart.analytics.dto.ForecastResponse;
import com.spendsmart.analytics.dto.HealthScoreResponse;
import com.spendsmart.analytics.dto.MonthlySummary;
import com.spendsmart.analytics.dto.TopCategory;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.events.NotificationEvent;
import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.repository.AnalyticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsServiceImpl Unit Tests")
class AnalyticsServiceImplTest {

    @Mock private AnalyticsRepository analyticsRepository;
    @Mock private ExpenseServiceClient expenseServiceClient;
    @Mock private IncomeServiceClient incomeServiceClient;
    @Mock private BudgetServiceClient budgetServiceClient;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private final Long userId = 1L;
    private final int year = 2026;
    private final int month = 4;

    @Test
    @DisplayName("generateMonthlySnapshot() - success with notification")
    void generateMonthlySnapshot_Success() {
        when(incomeServiceClient.getTotalIncomeByMonth(year, month))
                .thenReturn(ApiResponse.success("Success", BigDecimal.valueOf(5000)));
        when(expenseServiceClient.getTotalExpensesByMonth(year, month))
                .thenReturn(ApiResponse.success("Success", BigDecimal.valueOf(3000)));
        when(expenseServiceClient.getCategoryBreakdown(year, month))
                .thenReturn(ApiResponse.success("Success", Collections.singletonMap("Food", BigDecimal.valueOf(1000))));

        analyticsService.generateMonthlySnapshot(userId, year, month);

        verify(analyticsRepository).save(any(FinancialSnapshot.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("generateMonthlySnapshot() - should handle RabbitMQ failure gracefully")
    void generateMonthlySnapshot_RabbitMQFailure() {
        when(incomeServiceClient.getTotalIncomeByMonth(year, month))
                .thenReturn(ApiResponse.success("Success", BigDecimal.valueOf(5000)));
        when(expenseServiceClient.getTotalExpensesByMonth(year, month))
                .thenReturn(ApiResponse.success("Success", BigDecimal.valueOf(3000)));
        when(expenseServiceClient.getCategoryBreakdown(year, month))
                .thenReturn(ApiResponse.success("Success", Collections.emptyMap()));
        
        doThrow(new RuntimeException("MQ Down")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(NotificationEvent.class));

        assertDoesNotThrow(() -> analyticsService.generateMonthlySnapshot(userId, year, month));
        verify(analyticsRepository).save(any(FinancialSnapshot.class));
    }

    @Test
    @DisplayName("getMonthlySummary() - fetch from database")
    void getMonthlySummary_FromDatabase() {
        FinancialSnapshot snapshot = FinancialSnapshot.builder()
                .totalIncome(BigDecimal.valueOf(5000))
                .totalExpenses(BigDecimal.valueOf(3000))
                .build();
        when(analyticsRepository.findByUserIdAndYearAndMonth(userId, year, month)).thenReturn(Optional.of(snapshot));

        MonthlySummary summary = analyticsService.getMonthlySummary(userId, year, month);

        assertThat(summary.getTotalIncome()).isEqualTo(BigDecimal.valueOf(5000));
        verifyNoInteractions(incomeServiceClient);
    }

    @Test
    @DisplayName("getMonthlySummary() - calculate on the fly when not in DB")
    void getMonthlySummary_CalculateOnTheFly() {
        when(analyticsRepository.findByUserIdAndYearAndMonth(userId, year, month)).thenReturn(Optional.empty());
        when(incomeServiceClient.getTotalIncomeByMonth(year, month)).thenReturn(ApiResponse.success("ok", BigDecimal.valueOf(5000)));
        when(expenseServiceClient.getTotalExpensesByMonth(year, month)).thenReturn(ApiResponse.success("ok", BigDecimal.valueOf(3000)));

        MonthlySummary summary = analyticsService.getMonthlySummary(userId, year, month);

        assertThat(summary.getNetSavings()).isEqualTo(BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("getFinancialHealthScore() - excellent tier verification")
    void getFinancialHealthScore_Excellent() {
        setupHealthMocks(10000, 2000, 8000);
        HealthScoreResponse response = analyticsService.getFinancialHealthScore(userId, year, month);
        assertThat(response.getStatus()).isEqualTo("EXCELLENT");
        assertThat(response.getScore()).isGreaterThanOrEqualTo(80.0);
    }

    @Test
    @DisplayName("getFinancialHealthScore() - good tier verification")
    void getFinancialHealthScore_Good() {
        setupHealthMocks(5000, 2000, 3000, 1500); // 50% adherence
        HealthScoreResponse response = analyticsService.getFinancialHealthScore(userId, year, month);
        assertThat(response.getStatus()).isEqualTo("GOOD");
    }

    @Test
    @DisplayName("getFinancialHealthScore() - average tier verification")
    void getFinancialHealthScore_Average() {
        setupHealthMocks(5000, 3000, 3500, 2800); // 20% adherence
        HealthScoreResponse response = analyticsService.getFinancialHealthScore(userId, year, month);
        assertThat(response.getStatus()).isEqualTo("AVERAGE");
    }

    @Test
    @DisplayName("getFinancialHealthScore() - poor tier verification")
    void getFinancialHealthScore_Poor() {
        setupHealthMocks(5000, 4800, 4000, 3800); // Low adherence
        HealthScoreResponse response = analyticsService.getFinancialHealthScore(userId, year, month);
        assertThat(response.getStatus()).isEqualTo("POOR");
    }

    @Test
    @DisplayName("getSpendingForecast() - momentum increase logic")
    void getSpendingForecast_MomentumIncrease() {
        FinancialSnapshot s1 = FinancialSnapshot.builder().totalExpenses(BigDecimal.valueOf(3000)).build();
        FinancialSnapshot s2 = FinancialSnapshot.builder().totalExpenses(BigDecimal.valueOf(2000)).build();
        
        when(analyticsRepository.findByUserIdAndYearAndMonth(eq(userId), anyInt(), anyInt()))
                .thenReturn(Optional.of(s1))
                .thenReturn(Optional.of(s2))
                .thenReturn(Optional.empty());

        ForecastResponse response = analyticsService.getSpendingForecast(userId);
        assertThat(response.getForecastedExpenses()).isEqualByComparingTo("2625.00");
    }

    @Test
    @DisplayName("getTopSpendingCategories() - sort and limit")
    void getTopSpendingCategories_SortAndLimit() {
        Map<String, BigDecimal> data = new HashMap<>();
        data.put("Food", BigDecimal.valueOf(100));
        data.put("Rent", BigDecimal.valueOf(1000));
        data.put("Utils", BigDecimal.valueOf(200));
        
        when(expenseServiceClient.getCategoryBreakdown(anyInt(), anyInt())).thenReturn(ApiResponse.success("ok", data));

        List<TopCategory> result = analyticsService.getTopSpendingCategories(userId, 2);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Rent");
    }

    @Test
    @DisplayName("getYearlySummary() - should aggregate monthly snapshots")
    void getYearlySummary_Success() {
        FinancialSnapshot snapshot = FinancialSnapshot.builder()
                .totalIncome(BigDecimal.valueOf(5000))
                .totalExpenses(BigDecimal.valueOf(3000))
                .year(year)
                .month(1)
                .build();
        when(analyticsRepository.findByUserIdAndYear(userId, year)).thenReturn(Collections.singletonList(snapshot));

        List<MonthlySummary> result = analyticsService.getYearlySummary(userId, year);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalIncome()).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("getExpenseBreakdownByCategory() - should map BigDecimal to Double")
    void getExpenseBreakdownByCategory_Success() {
        Map<String, BigDecimal> breakdown = Map.of("Food", BigDecimal.valueOf(100.50));
        when(expenseServiceClient.getCategoryBreakdown(year, month)).thenReturn(ApiResponse.success("ok", breakdown));

        Map<String, Double> result = analyticsService.getExpenseBreakdownByCategory(userId, year, month);

        assertThat(result).containsEntry("Food", 100.50);
    }

    @Test
    @DisplayName("getIncomeVsExpenseTrend() - should calculate trend correctly")
    void getIncomeVsExpenseTrend_Success() {
        FinancialSnapshot s1 = FinancialSnapshot.builder()
                .month(1)
                .totalIncome(BigDecimal.valueOf(5000))
                .totalExpenses(BigDecimal.valueOf(3000))
                .build();
        when(analyticsRepository.findByUserIdAndYear(userId, year)).thenReturn(Collections.singletonList(s1));

        Map<String, Map<String, Double>> result = analyticsService.getIncomeVsExpenseTrend(userId, year);

        assertThat(result).containsKey("January");
        assertThat(result.get("January")).containsEntry("income", 5000.0);
    }

    @Test
    @DisplayName("getSavingsRateTrend() - should calculate savings rate trend")
    void getSavingsRateTrend_Success() {
        FinancialSnapshot s1 = FinancialSnapshot.builder()
                .month(1)
                .savingsRate(BigDecimal.valueOf(40.0))
                .build();
        when(analyticsRepository.findByUserIdAndYear(userId, year)).thenReturn(Collections.singletonList(s1));

        Map<String, Double> result = analyticsService.getSavingsRateTrend(userId, year);

        assertThat(result).containsKey("January");
        assertThat(result).containsValue(40.0);
    }

    @Test
    @DisplayName("getDailyExpenseTrend() - should return daily data")
    void getDailyExpenseTrend_Success() {
        Map<String, BigDecimal> dailyData = Map.of("2026-04-01", BigDecimal.valueOf(50.0));
        when(expenseServiceClient.getDailyTrend(year, month)).thenReturn(ApiResponse.success("ok", dailyData));

        Map<String, Double> result = analyticsService.getDailyExpenseTrend(userId, year, month);

        assertThat(result).containsEntry("2026-04-01", 50.0);
    }

    @Test
    @DisplayName("getCashflowData() - should return empty map as per current impl")
    void getCashflowData_ReturnsEmptyMap() {
        assertThat(analyticsService.getCashflowData(userId, year, month)).isEmpty();
    }

    @Test
    @DisplayName("getIncomeVsExpenseTrend() - should handle empty trend")
    void getIncomeVsExpenseTrend_Empty() {
        when(analyticsRepository.findByUserIdAndYear(userId, year)).thenReturn(Collections.emptyList());
        when(incomeServiceClient.getTotalIncomeByMonth(anyInt(), anyInt())).thenReturn(ApiResponse.success("ok", BigDecimal.valueOf(1000)));
        when(expenseServiceClient.getTotalExpensesByMonth(anyInt(), anyInt())).thenReturn(ApiResponse.success("ok", BigDecimal.valueOf(500)));

        Map<String, Map<String, Double>> result = analyticsService.getIncomeVsExpenseTrend(userId, year);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("getSpendingForecast() - should handle insufficient data")
    void getSpendingForecast_InsufficientData() {
        when(analyticsRepository.findByUserIdAndYearAndMonth(eq(userId), anyInt(), anyInt())).thenReturn(Optional.empty());

        ForecastResponse response = analyticsService.getSpendingForecast(userId);
        assertThat(response.getForecastedExpenses()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("fetch methods - should handle error responses")
    void fetchMethods_ErrorResponse() {
        when(incomeServiceClient.getTotalIncomeByMonth(anyInt(), anyInt())).thenReturn(ApiResponse.error("fail"));
        
        MonthlySummary summary = analyticsService.getMonthlySummary(userId, year, month);
        assertThat(summary.getTotalIncome()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("fetch methods - should handle null or error data")
    void fetchMethods_FullErrors() {
        when(incomeServiceClient.getTotalIncomeByMonth(anyInt(), anyInt())).thenReturn(ApiResponse.error("fail"));
        when(expenseServiceClient.getCategoryBreakdown(anyInt(), anyInt())).thenReturn(ApiResponse.error("fail"));
        when(budgetServiceClient.getTotalBudgetByMonth(anyInt(), anyInt())).thenReturn(ApiResponse.error("fail"));
        when(expenseServiceClient.getDailyTrend(anyInt(), anyInt())).thenReturn(ApiResponse.error("fail"));

        assertThat(analyticsService.getExpenseBreakdownByCategory(userId, year, month)).isEmpty();
        assertThat(analyticsService.getTopSpendingCategories(userId, 5)).isEmpty();
        assertThat(analyticsService.getDailyExpenseTrend(userId, year, month)).isEmpty();
        
        HealthScoreResponse health = analyticsService.getFinancialHealthScore(userId, year, month);
        assertThat(health.getScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("calculateMomentum() - should handle varying lists")
    void calculateMomentum_Logic() {
        // This is a private method called by getSpendingForecast
        // We can trigger it by providing snapshots with different values
        FinancialSnapshot s1 = FinancialSnapshot.builder().totalExpenses(BigDecimal.valueOf(100)).build();
        FinancialSnapshot s2 = FinancialSnapshot.builder().totalExpenses(BigDecimal.valueOf(100)).build();
        FinancialSnapshot s3 = FinancialSnapshot.builder().totalExpenses(BigDecimal.valueOf(100)).build();
        
        when(analyticsRepository.findByUserIdAndYearAndMonth(eq(userId), anyInt(), anyInt()))
                .thenReturn(Optional.of(s1))
                .thenReturn(Optional.of(s2))
                .thenReturn(Optional.of(s3));
                
        ForecastResponse response = analyticsService.getSpendingForecast(userId);
        assertThat(response.getConfidence()).isEqualTo("HIGH");
    }

    private void setupHealthMocks(double income, double expense, double budgetLimit, double budgetSpent) {
        when(incomeServiceClient.getTotalIncomeByMonth(anyInt(), anyInt()))
                .thenReturn(ApiResponse.success("ok", BigDecimal.valueOf(income)));
        when(expenseServiceClient.getTotalExpensesByMonth(anyInt(), anyInt()))
                .thenReturn(ApiResponse.success("ok", BigDecimal.valueOf(expense)));
        
        BudgetDto budget = BudgetDto.builder()
                .limitAmount(BigDecimal.valueOf(budgetLimit))
                .spentAmount(BigDecimal.valueOf(budgetSpent))
                .build();
        when(budgetServiceClient.getActiveBudgets())
                .thenReturn(ApiResponse.success("ok", Collections.singletonList(budget)));
    }

    private void setupHealthMocks(double income, double expense, double budgetLimit) {
        setupHealthMocks(income, expense, budgetLimit, 0);
    }

    @Test
    @DisplayName("getSpendingForecast() - momentum decrease logic")
    void getSpendingForecast_MomentumDecrease() {
        FinancialSnapshot s1 = FinancialSnapshot.builder().totalExpenses(BigDecimal.valueOf(2000)).build();
        FinancialSnapshot s2 = FinancialSnapshot.builder().totalExpenses(BigDecimal.valueOf(3000)).build();
        
        when(analyticsRepository.findByUserIdAndYearAndMonth(eq(userId), anyInt(), anyInt()))
                .thenReturn(Optional.of(s1))
                .thenReturn(Optional.of(s2))
                .thenReturn(Optional.empty());

        ForecastResponse response = analyticsService.getSpendingForecast(userId);
        assertThat(response.getForecastedExpenses()).isEqualByComparingTo("2375.00");
    }

    @Test
    @DisplayName("getFinancialHealthScore() - zero budget should not break")
    void getFinancialHealthScore_ZeroBudget() {
        setupHealthMocks(5000, 2000, 0); 
        HealthScoreResponse response = analyticsService.getFinancialHealthScore(userId, year, month);
        assertThat(response.getScore()).isGreaterThan(0);
    }
}
