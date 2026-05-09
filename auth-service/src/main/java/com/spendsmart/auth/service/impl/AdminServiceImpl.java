package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.client.ExpenseClient;
import com.spendsmart.auth.client.IncomeClient;
import com.spendsmart.auth.constants.AppConstants;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.events.NotificationEvent;
import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.dto.admin.PlatformAnalytics;
import com.spendsmart.auth.dto.admin.TopUserDTO;
import com.spendsmart.auth.dto.admin.TransactionDTO;
import com.spendsmart.auth.entity.User;
import com.spendsmart.shared.exception.ResourceNotFoundException;
import com.spendsmart.auth.mapper.UserMapper;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ExpenseClient expenseClient;
    private final IncomeClient incomeClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public List<UserProfileResponse> getAllUsers() {
        log.info("Admin: Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toProfileResponse)
                .toList();
    }

    @Override
    @Transactional
    public void suspendUser(Long userId) {
        log.info("Admin: Suspending user {}", userId);
        User user = findUserOrThrow(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUser(Long userId) {
        log.info("Admin: Activating user {}", userId);
        User user = findUserOrThrow(userId);
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin: Deleting user {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        log.info("Admin: Fetching all platform transactions");
        List<TransactionDTO> transactions = new ArrayList<>();

        try {
            // Fetch Expenses
            ApiResponse<Page<Map<String, Object>>> expenseResp = expenseClient.getAllExpenses(0, 1000, "date", "desc");
            if (expenseResp.isSuccess() && expenseResp.getData() != null) {
                for (Map<String, Object> e : expenseResp.getData().getContent()) {
                    transactions.add(mapToTransaction(e, "EXPENSE"));
                }
            }

            // Fetch Incomes
            ApiResponse<Page<Map<String, Object>>> incomeResp = incomeClient.getAllIncomes(0, 1000, new String[]{"date", "desc"});
            if (incomeResp.isSuccess() && incomeResp.getData() != null) {
                for (Map<String, Object> i : incomeResp.getData().getContent()) {
                    transactions.add(mapToTransaction(i, "INCOME"));
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch transactions from other services: {}", e.getMessage());
        }

        transactions.sort(Comparator.comparing(TransactionDTO::getDate).reversed());
        return transactions;
    }

    @Override
    public PlatformAnalytics getPlatformAnalytics() {
        log.info("Admin: Calculating platform analytics");
        long userCount = userRepository.count();
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        long transactionCount = 0;

        try {
            ApiResponse<Map<String, Object>> eStats = expenseClient.getGlobalStats();
            if (eStats.isSuccess() && eStats.getData() != null) {
                totalExpenses = new BigDecimal(eStats.getData().get("totalAmount").toString());
                transactionCount += Long.parseLong(eStats.getData().get("totalCount").toString());
            }

            ApiResponse<Map<String, Object>> iStats = incomeClient.getGlobalStats();
            if (iStats.isSuccess() && iStats.getData() != null) {
                totalIncome = new BigDecimal(iStats.getData().get("totalAmount").toString());
                transactionCount += Long.parseLong(iStats.getData().get("totalCount").toString());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch global stats: {}", e.getMessage());
        }

        BigDecimal avgSpending = userCount > 0 
                ? totalExpenses.divide(BigDecimal.valueOf(userCount), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        return PlatformAnalytics.builder()
                .totalUsers(userCount)
                .totalTransactions(transactionCount)
                .totalExpenses(totalExpenses)
                .totalIncome(totalIncome)
                .avgSpendingPerUser(avgSpending)
                .userRegistrationTrend(getRegistrationTrend())
                .build();
    }

    @Override
    public List<TopUserDTO> getTopSpendingUsers() {
        // Simple implementation: list all users and their total expenses
        // In a real app, this would be a specialized aggregation query
        // This is a placeholder logic as we'd need a bulk "get totals by user list" endpoint
        // For now, we return empty or basic info
        return List.of();
    }

    @Override
    public void sendGlobalNotification(String title, String message, String severity) {
        log.info("Admin: Sending global notification: {}", title);
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(null) // null means global/system-wide for all users
                .type("SYSTEM")
                .severity(severity != null ? severity : "INFO")
                .title(title)
                .message(message)
                .build();
        
        rabbitTemplate.convertAndSend(AppConstants.NOTIFICATION_EXCHANGE, AppConstants.NOTIFICATION_ROUTING_KEY, event);
    }

    @Override
    public byte[] exportPlatformReport() {
        log.info("Admin: Exporting platform report");
        // Simple JSON export for now
        PlatformAnalytics analytics = getPlatformAnalytics();
        List<UserProfileResponse> users = getAllUsers();
        
        Map<String, Object> report = new HashMap<>();
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("analytics", analytics);
        report.put("userCount", users.size());
        
        return report.toString().getBytes();
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private TransactionDTO mapToTransaction(Map<String, Object> map, String type) {
        return TransactionDTO.builder()
                .id(map.get(type.equalsIgnoreCase("EXPENSE") ? "expenseId" : "incomeId").toString())
                .userId(Long.parseLong(map.get("userId").toString()))
                .type(type)
                .amount(new BigDecimal(map.get("amount").toString()))
                .category(resolveCategory(map))
                .description(resolveDescription(map))
                .date(parseFlexibleDate(map.get("date").toString()))
                .build();
    }

    private String resolveCategory(Map<String, Object> map) {
        Object categoryName = map.get("categoryName");
        if (categoryName != null) {
            return categoryName.toString();
        }

        Object source = map.get("source");
        return source != null ? source.toString() : "Other";
    }

    private String resolveDescription(Map<String, Object> map) {
        Object title = map.get("title");
        if (title != null) {
            return title.toString();
        }

        Object description = map.get("description");
        return description != null ? description.toString() : "";
    }

    private LocalDateTime parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return LocalDateTime.now();
        try {
            // Try ISO Date Time first (e.g. 2026-04-27T10:00:00)
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            try {
                // Try simple date (e.g. 2026-04-27)
                return java.time.LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
            } catch (Exception e2) {
                log.warn("Failed to parse date: {}", dateStr);
                return LocalDateTime.now();
            }
        }
    }

    private Map<String, Long> getRegistrationTrend() {
        return userRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));
    }
}
