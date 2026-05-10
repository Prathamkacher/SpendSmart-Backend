package com.spendsmart.analytics.service.impl;

import com.spendsmart.analytics.client.BudgetServiceClient;
import com.spendsmart.analytics.client.ExpenseServiceClient;
import com.spendsmart.analytics.client.IncomeServiceClient;
import com.spendsmart.analytics.config.RabbitMQConfig;
import com.spendsmart.analytics.dto.*;
import com.spendsmart.analytics.entity.FinancialSnapshot;
import com.spendsmart.analytics.repository.AnalyticsRepository;
import com.spendsmart.analytics.service.AnalyticsService;
import com.spendsmart.shared.amqp.NotificationPublisherRabbitConfig;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final String NONE = "None";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final double EXCELLENT_THRESHOLD = 80.0;
    private static final double GOOD_THRESHOLD = 60.0;
    private static final double AVERAGE_THRESHOLD = 40.0;

    private final AnalyticsRepository analyticsRepository;
    private final ExpenseServiceClient expenseServiceClient;
    private final IncomeServiceClient incomeServiceClient;
    private final BudgetServiceClient budgetServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void generateMonthlySnapshot(Long userId, int year, int month) {
        log.info("Generating monthly snapshot for user {} - {}/{}", userId, month, year);

        BigDecimal totalIncome = fetchTotalIncome(year, month);
        BigDecimal totalExpenses = fetchTotalExpenses(year, month);
        Map<String, BigDecimal> categoryBreakdown = fetchCategoryBreakdown(year, month);

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalExpenses);

        String topCategory = categoryBreakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(NONE);

        FinancialSnapshot snapshot = FinancialSnapshot.builder()
                .userId(userId)
                .period("MONTHLY")
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .savingsRate(savingsRate)
                .topCategory(topCategory)
                .build();

        analyticsRepository.save(snapshot);
        sendSummaryNotification(userId, month, totalIncome, totalExpenses, netSavings);
    }

    private void sendSummaryNotification(Long userId, int month, BigDecimal income, BigDecimal expenses, BigDecimal savings) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .recipientId(userId)
                    .type("MONTHLY_SUMMARY")
                    .severity("INFO")
                    .title("Monthly Summary - " + getMonthName(month))
                    .message(String.format("Your monthly summary for %s is ready. Total Income: %s, Total Expenses: %s, Net Savings: %s.", 
                        getMonthName(month), income, expenses, savings))
                    .relatedId((long) month)
                    .relatedType("MONTHLY_SUMMARY")
                    .build();
            
            rabbitTemplate.convertAndSend(NotificationPublisherRabbitConfig.NOTIFICATION_EXCHANGE, NotificationPublisherRabbitConfig.NOTIFICATION_ROUTING_KEY, event);
            log.info("Sent monthly summary event to RabbitMQ for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to send monthly summary notification to RabbitMQ", e);
        }
    }

    @Override
    public MonthlySummary getMonthlySummary(Long userId, int year, int month) {
        return analyticsRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(s -> MonthlySummary.builder()
                        .totalIncome(s.getTotalIncome())
                        .totalExpenses(s.getTotalExpenses())
                        .netSavings(s.getNetSavings())
                        .savingsRate(s.getSavingsRate())
                        .topCategory(s.getTopCategory())
                        .build())
                .orElseGet(() -> {
                    BigDecimal income = fetchTotalIncome(year, month);
                    BigDecimal expenses = fetchTotalExpenses(year, month);
                    return MonthlySummary.builder()
                            .totalIncome(income)
                            .totalExpenses(expenses)
                            .netSavings(income.subtract(expenses))
                            .savingsRate(calculateSavingsRate(income, expenses))
                            .build();
                });
    }

    @Override
    public List<MonthlySummary> getYearlySummary(Long userId, int year) {
        return analyticsRepository.findByUserIdAndYear(userId, year).stream()
                .map(s -> MonthlySummary.builder()
                        .totalIncome(s.getTotalIncome())
                        .totalExpenses(s.getTotalExpenses())
                        .netSavings(s.getNetSavings())
                        .savingsRate(s.getSavingsRate())
                        .topCategory(s.getTopCategory())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> getExpenseBreakdownByCategory(Long userId, int year, int month) {
        ApiResponse<Map<String, BigDecimal>> response = expenseServiceClient.getCategoryBreakdown(year, month);
        if (isResponseValid(response)) {
            return response.getData().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Map<String, Double>> getIncomeVsExpenseTrend(Long userId, int year) {
        List<FinancialSnapshot> snapshots = analyticsRepository.findByUserIdAndYear(userId, year);
        Map<String, Map<String, Double>> trend = new LinkedHashMap<>();

        for (FinancialSnapshot s : snapshots) {
            Map<String, Double> data = new HashMap<>();
            data.put("income", s.getTotalIncome().doubleValue());
            data.put("expenses", s.getTotalExpenses().doubleValue());
            trend.put(getMonthName(s.getMonth()), data);
        }

        if (trend.isEmpty()) {
            int currentMonth = LocalDate.now().getMonthValue();
            for (int m = 1; m <= currentMonth; m++) {
                BigDecimal income = fetchTotalIncome(year, m);
                BigDecimal expenses = fetchTotalExpenses(year, m);
                if (income.compareTo(BigDecimal.ZERO) > 0 || expenses.compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Double> monthData = new HashMap<>();
                    monthData.put("income", income.doubleValue());
                    monthData.put("expenses", expenses.doubleValue());
                    trend.put(getMonthName(m), monthData);
                }
            }
        }

        return trend;
    }

    @Override
    public Map<String, Double> getSavingsRateTrend(Long userId, int year) {
        List<FinancialSnapshot> snapshots = analyticsRepository.findByUserIdAndYear(userId, year);
        return snapshots.stream()
                .collect(Collectors.toMap(
                        s -> getMonthName(s.getMonth()),
                        s -> s.getSavingsRate().doubleValue(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    @Override
    public List<TopCategory> getTopSpendingCategories(Long userId, int limit) {
        LocalDate now = LocalDate.now();
        ApiResponse<Map<String, BigDecimal>> response = expenseServiceClient.getCategoryBreakdown(now.getYear(), now.getMonthValue());
        
        if (isResponseValid(response)) {
            return response.getData().entrySet().stream()
                    .map(e -> TopCategory.builder()
                            .categoryName(e.getKey())
                            .totalSpent(e.getValue())
                            .build())
                    .sorted(Comparator.comparing(TopCategory::getTotalSpent).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, Double> getDailyExpenseTrend(Long userId, int year, int month) {
        ApiResponse<Map<String, BigDecimal>> response = expenseServiceClient.getDailyTrend(year, month);
        if (isResponseValid(response)) {
            return response.getData().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue(), (v1, v2) -> v1, TreeMap::new));
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Double> getCashflowData(Long userId, int year, int month) {
        return Collections.emptyMap();
    }

    @Override
    public ForecastResponse getSpendingForecast(Long userId) {
        LocalDate now = LocalDate.now();
        List<FinancialSnapshot> last3Months = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            LocalDate date = now.minusMonths(i);
            analyticsRepository.findByUserIdAndYearAndMonth(userId, date.getYear(), date.getMonthValue())
                    .ifPresent(last3Months::add);
        }

        if (last3Months.isEmpty()) {
            return ForecastResponse.builder()
                    .forecastedExpenses(BigDecimal.ZERO)
                    .confidence("LOW")
                    .message("Not enough data to forecast. Please use the app for at least 3 months.")
                    .build();
        }

        BigDecimal avgExpenses = last3Months.stream()
                .map(FinancialSnapshot::getTotalExpenses)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(last3Months.size()), 2, RoundingMode.HALF_UP);

        BigDecimal momentum = calculateMomentum(last3Months);
        BigDecimal forecast = avgExpenses.multiply(momentum).setScale(2, RoundingMode.HALF_UP);

        return ForecastResponse.builder()
                .forecastedExpenses(forecast)
                .confidence(last3Months.size() == 3 ? "HIGH" : "MEDIUM")
                .message("Based on your last " + last3Months.size() + " months of spending.")
                .build();
    }

    private BigDecimal calculateMomentum(List<FinancialSnapshot> history) {
        if (history.size() >= 2) {
            BigDecimal lastMonth = history.get(0).getTotalExpenses();
            BigDecimal prevMonth = history.get(1).getTotalExpenses();
            if (lastMonth.compareTo(prevMonth) > 0) return BigDecimal.valueOf(1.05);
            if (lastMonth.compareTo(prevMonth) < 0) return BigDecimal.valueOf(0.95);
        }
        return BigDecimal.ONE;
    }

    @Override
    public HealthScoreResponse getFinancialHealthScore(Long userId, int year, int month) {
        BigDecimal income = fetchTotalIncome(year, month);
        BigDecimal expenses = fetchTotalExpenses(year, month);
        BigDecimal budget = fetchTotalBudget(year, month);

        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return HealthScoreResponse.builder().score(0).status("POOR").insight("No income recorded.").build();
        }

        double savingsRate = income.subtract(expenses).divide(income, 4, RoundingMode.HALF_UP).doubleValue() * 100;
        
        double budgetAdherence = 100;
        try {
            ApiResponse<java.util.List<BudgetDto>> activeBudgetsResponse = budgetServiceClient.getActiveBudgets();
            if (isResponseValid(activeBudgetsResponse) && !activeBudgetsResponse.getData().isEmpty()) {
                BigDecimal totalBudgetLimit = BigDecimal.ZERO;
                BigDecimal totalBudgetSpent = BigDecimal.ZERO;
                for (BudgetDto b : activeBudgetsResponse.getData()) {
                    if (b.getLimitAmount() != null) totalBudgetLimit = totalBudgetLimit.add(b.getLimitAmount());
                    if (b.getSpentAmount() != null) totalBudgetSpent = totalBudgetSpent.add(b.getSpentAmount());
                }
                if (totalBudgetLimit.compareTo(BigDecimal.ZERO) > 0) {
                    budgetAdherence = totalBudgetLimit.subtract(totalBudgetSpent)
                            .divide(totalBudgetLimit, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                }
            }
        } catch (Exception e) {
            log.error("Error calculating active budget adherence", e);
        }

        double expenseRatio = expenses.divide(income, 4, RoundingMode.HALF_UP).doubleValue() * 100;

        double savingsScore = Math.max(0, Math.min(100, savingsRate * 2));
        double budgetScore = Math.max(0, Math.min(100, budgetAdherence));
        double ratioScore = Math.max(0, Math.min(100, 100 - expenseRatio));

        double totalScore = (savingsScore * 0.4) + (budgetScore * 0.4) + (ratioScore * 0.2);

        String status;
        String insight;
        if (totalScore >= EXCELLENT_THRESHOLD) {
            status = "EXCELLENT";
            insight = "Great job! Your savings and budget discipline are top-notch.";
        } else if (totalScore >= GOOD_THRESHOLD) {
            status = "GOOD";
            insight = "You're doing well. Try to reduce discretionary spending.";
        } else if (totalScore >= AVERAGE_THRESHOLD) {
            status = "AVERAGE";
            insight = "Your finances are stable but could be better.";
        } else {
            status = "POOR";
            insight = "Warning: Your expenses are high relative to your income.";
        }

        return HealthScoreResponse.builder()
                .score(Math.round(totalScore))
                .status(status)
                .insight(insight)
                .build();
    }

    private BigDecimal fetchTotalIncome(int year, int month) {
        try {
            ApiResponse<BigDecimal> response = incomeServiceClient.getTotalIncomeByMonth(year, month);
            return (isResponseValid(response)) ? response.getData() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error fetching total income", e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal fetchTotalExpenses(int year, int month) {
        try {
            ApiResponse<BigDecimal> response = expenseServiceClient.getTotalExpensesByMonth(year, month);
            return (isResponseValid(response)) ? response.getData() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error fetching total expenses", e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal fetchTotalBudget(int year, int month) {
        try {
            ApiResponse<BigDecimal> response = budgetServiceClient.getTotalBudgetByMonth(year, month);
            return (isResponseValid(response)) ? response.getData() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error fetching total budget", e);
            return BigDecimal.ZERO;
        }
    }

    private Map<String, BigDecimal> fetchCategoryBreakdown(int year, int month) {
        try {
            ApiResponse<Map<String, BigDecimal>> response = expenseServiceClient.getCategoryBreakdown(year, month);
            return (isResponseValid(response)) ? response.getData() : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Error fetching category breakdown", e);
            return Collections.emptyMap();
        }
    }

    private boolean isResponseValid(ApiResponse<?> response) {
        return response != null && response.isSuccess() && response.getData() != null;
    }

    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return income.subtract(expenses).divide(income, 4, RoundingMode.HALF_UP).multiply(HUNDRED);
    }

    private String getMonthName(int month) {
        String name = java.time.Month.of(month).name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
