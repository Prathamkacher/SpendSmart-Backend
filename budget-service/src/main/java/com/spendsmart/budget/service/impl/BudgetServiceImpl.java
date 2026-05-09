package com.spendsmart.budget.service.impl;

import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.BudgetResponse;
import com.spendsmart.budget.dto.BudgetUpdateRequest;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.exception.BudgetNotFoundException;
import com.spendsmart.budget.mapper.BudgetMapper;
import com.spendsmart.budget.repository.BudgetRepository;
import com.spendsmart.budget.service.BudgetService;
import com.spendsmart.shared.amqp.NotificationPublisherRabbitConfig;
import com.spendsmart.shared.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        log.info("Creating budget for userId={}, name='{}'", userId, request.getName());

        // Deactivate existing active budget for this category if any
        budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, request.getCategoryId())
                .ifPresent(existing -> {
                    log.info("Deactivating existing budget id={} for category={}", existing.getBudgetId(), request.getCategoryId());
                    existing.setIsActive(false);
                    budgetRepository.save(existing);
                });

        Budget budget = budgetMapper.toEntity(request);
        budget.setUserId(userId);
        budget.setIsActive(true);
        budget.setSpentAmount(BigDecimal.ZERO);

        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponse(saved);
    }

    @Override
    public BudgetResponse getBudgetById(Long userId, Long budgetId) {
        Budget budget = findBudgetOrThrow(budgetId, userId);
        return budgetMapper.toResponse(budget);
    }

    @Override
    public List<BudgetResponse> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    @Override
    public List<BudgetResponse> getActiveBudgets(Long userId) {
        return budgetRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long userId, Long budgetId, BudgetRequest request) {
        Budget existing = findBudgetOrThrow(budgetId, userId);
        budgetMapper.updateEntityFromRequest(request, existing);
        Budget updated = budgetRepository.save(existing);
        return budgetMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = findBudgetOrThrow(budgetId, userId);
        budgetRepository.delete(budget);
    }

    @Override
    @Transactional
    public void updateSpentAmount(BudgetUpdateRequest updateRequest) {
        if (updateRequest.getAmount() == null || updateRequest.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            log.info("Update amount is zero or null, skipping spent amount update.");
            return;
        }

        log.info("Updating spent amount for userId={}, categoryId={}, delta={}", 
                updateRequest.getUserId(), updateRequest.getCategoryId(), updateRequest.getAmount());

        int updatedCount = budgetRepository.updateSpentAmount(
                updateRequest.getUserId(), 
                updateRequest.getCategoryId(), 
                updateRequest.getAmount()
        );

        if (updatedCount > 0) {
            log.info("Atomic update successful. Checking for alerts...");
            checkAlerts(updateRequest.getUserId(), updateRequest.getCategoryId());
        } else {
            log.warn("No active budget found for userId={} and categoryId={}", 
                    updateRequest.getUserId(), updateRequest.getCategoryId());
        }
    }

    @Override
    public void resetExpiredBudgets() {
        LocalDate today = LocalDate.now();
        List<Budget> expired = budgetRepository.findExpiredBudgets(today);
        log.info("Found {} expired budgets to reset", expired.size());

        for (Budget budget : expired) {
            budget.setIsActive(false);
            budgetRepository.save(budget);
            
            // Logic to create new budget for next period could go here
            // For now we just deactivate
            log.info("Deactivated expired budget id={}", budget.getBudgetId());
        }
    }

    @Override
    public BigDecimal getTotalBudgetByMonth(Long userId, int year, int month) {
        log.debug("Calculating total budget for userId={}, month={}/{}", userId, year, month);
        // For simplicity, we sum all active budgets for the user
        // A more advanced version would check if the budget period covers the requested month
        return budgetRepository.sumLimitAmountByUserId(userId);
    }

    private void checkAlerts(Long userId, Long categoryId) {
        budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, categoryId)
                .ifPresent(budget -> {
                    BigDecimal limit = budget.getLimitAmount();
                    BigDecimal spent = budget.getSpentAmount();
                    
                    if (limit.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal thresholdPercent = new BigDecimal(budget.getAlertThreshold()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                        BigDecimal alertAmount = limit.multiply(thresholdPercent);

                        if (spent.compareTo(limit) >= 0) {
                            sendNotification(userId, budget, "CRITICAL", "Budget Exceeded!", 
                                "You have exceeded your budget '" + budget.getName() + "'. Spent: " + spent);
                        } else if (spent.compareTo(alertAmount) >= 0) {
                            sendNotification(userId, budget, "WARNING", "Budget Threshold Reached", 
                                "You have reached " + budget.getAlertThreshold() + "% of your budget '" + budget.getName() + "'.");
                        }
                    }
                });
    }

    private void sendNotification(Long userId, Budget budget, String severity, String title, String message) {
        log.info("Sending {} alert for budget {}: {}", severity, budget.getBudgetId(), message);
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(userId)
                .type("BUDGET_ALERT")
                .severity(severity)
                .title(title)
                .message(message)
                .relatedId(budget.getBudgetId())
                .relatedType("BUDGET")
                .build();
        try {
            rabbitTemplate.convertAndSend(NotificationPublisherRabbitConfig.NOTIFICATION_EXCHANGE, NotificationPublisherRabbitConfig.NOTIFICATION_ROUTING_KEY, event);
        } catch (Exception e) {
            log.error("RabbitMQ alert error for budget {}: {}", budget.getBudgetId(), e.getMessage());
        }
    }

    private Budget findBudgetOrThrow(Long budgetId, Long userId) {
        return budgetRepository.findById(budgetId)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));
    }
}
