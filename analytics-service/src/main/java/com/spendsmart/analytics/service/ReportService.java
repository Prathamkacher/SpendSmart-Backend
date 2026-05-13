package com.spendsmart.analytics.service;

import com.spendsmart.analytics.dto.MonthlySummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for generating financial reports.
 * Currently supports CSV report generation for monthly data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final AnalyticsService analyticsService;

    public String generateMonthlyReportCsv(Long userId, int year, int month) {
        log.info("Generating CSV report for user {} for {}/{}", userId, month, year);

        MonthlySummary summary = analyticsService.getMonthlySummary(userId, year, month);
        Map<String, Double> categoryBreakdown = analyticsService.getExpenseBreakdownByCategory(userId, year, month);

        StringBuilder csvBuilder = new StringBuilder();
        
        // Report Header
        csvBuilder.append("SpendSmart Financial Report").append(System.lineSeparator());
        csvBuilder.append(String.format("Period: %02d/%d%n%n", month, year));

        // Summary Section
        csvBuilder.append("Summary").append(System.lineSeparator());
        csvBuilder.append("Metric,Amount").append(System.lineSeparator());
        csvBuilder.append(String.format("Total Income,%.2f%n", summary.getTotalIncome()));
        csvBuilder.append(String.format("Total Expenses,%.2f%n", summary.getTotalExpenses()));
        csvBuilder.append(String.format("Net Savings,%.2f%n", summary.getNetSavings()));
        csvBuilder.append(String.format("Savings Rate (%%),%.2f%%%n%n", summary.getSavingsRate()));

        // Category Breakdown Section
        csvBuilder.append("Expense Breakdown by Category").append(System.lineSeparator());
        csvBuilder.append("Category,Amount").append(System.lineSeparator());
        
        if (categoryBreakdown == null || categoryBreakdown.isEmpty()) {
            csvBuilder.append("No expenses recorded for this month.").append(System.lineSeparator());
        } else {
            for (Map.Entry<String, Double> entry : categoryBreakdown.entrySet()) {
                csvBuilder.append(String.format("%s,%.2f%n", entry.getKey(), entry.getValue()));
            }
        }

        return csvBuilder.toString();
    }
}
