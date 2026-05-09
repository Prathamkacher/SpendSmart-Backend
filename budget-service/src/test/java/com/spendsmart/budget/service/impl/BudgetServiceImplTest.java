package com.spendsmart.budget.service.impl;

import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.BudgetResponse;
import com.spendsmart.budget.dto.BudgetUpdateRequest;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.events.NotificationEvent;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.exception.BudgetNotFoundException;
import com.spendsmart.budget.mapper.BudgetMapper;
import com.spendsmart.budget.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetServiceImpl Unit Tests")
class BudgetServiceImplTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private BudgetMapper budgetMapper;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private Budget testBudget;
    private BudgetRequest budgetRequest;
    private final Long userId = 1L;
    private final Long budgetId = 1L;

    @BeforeEach
    void setUp() {
        testBudget = Budget.builder()
                .budgetId(budgetId)
                .userId(userId)
                .categoryId(10L)
                .name("Monthly Food")
                .limitAmount(new BigDecimal("1000.00"))
                .spentAmount(new BigDecimal("200.00"))
                .alertThreshold(80)
                .isActive(true)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .build();

        budgetRequest = new BudgetRequest();
        budgetRequest.setName("Monthly Food");
        budgetRequest.setLimitAmount(new BigDecimal("1000.00"));
        budgetRequest.setCategoryId(10L);
    }

    @Test
    @DisplayName("createBudget() - should deactivate existing and save new")
    void createBudget_ShouldDeactivateExistingAndSave() {
        when(budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, 10L)).thenReturn(Optional.of(testBudget));
        when(budgetMapper.toEntity(any())).thenReturn(testBudget);
        when(budgetRepository.save(any())).thenReturn(testBudget);
        when(budgetMapper.toResponse(any())).thenReturn(new BudgetResponse());

        BudgetResponse response = budgetService.createBudget(userId, budgetRequest);

        assertThat(response).isNotNull();
        verify(budgetRepository, times(2)).save(any()); // Once for deactivating, once for saving new
    }

    @Test
    @DisplayName("getBudgetById() - should return budget when found")
    void getBudgetById_ShouldReturnResponse() {
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(testBudget));
        when(budgetMapper.toResponse(testBudget)).thenReturn(new BudgetResponse());

        BudgetResponse response = budgetService.getBudgetById(userId, budgetId);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("getBudgetById() - should throw exception when not found")
    void getBudgetById_NotFound_ShouldThrowException() {
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudgetById(userId, budgetId))
                .isInstanceOf(BudgetNotFoundException.class);
    }

    @Test
    @DisplayName("getActiveBudgets() - should return active budgets")
    void getActiveBudgets_ShouldReturnList() {
        when(budgetRepository.findByUserIdAndIsActiveTrue(userId)).thenReturn(Collections.singletonList(testBudget));
        when(budgetMapper.toResponse(any())).thenReturn(new BudgetResponse());

        List<BudgetResponse> result = budgetService.getActiveBudgets(userId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getBudgetsByUser() - should return all mapped budgets")
    void getBudgetsByUser_ShouldReturnList() {
        when(budgetRepository.findByUserId(userId)).thenReturn(Collections.singletonList(testBudget));
        when(budgetMapper.toResponse(testBudget)).thenReturn(new BudgetResponse());

        List<BudgetResponse> result = budgetService.getBudgetsByUser(userId);

        assertThat(result).hasSize(1);
        verify(budgetRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("updateBudget() - should update existing budget")
    void updateBudget_ShouldSaveUpdatedBudget() {
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(testBudget)).thenReturn(testBudget);
        when(budgetMapper.toResponse(testBudget)).thenReturn(new BudgetResponse());

        BudgetResponse response = budgetService.updateBudget(userId, budgetId, budgetRequest);

        assertThat(response).isNotNull();
        verify(budgetMapper).updateEntityFromRequest(budgetRequest, testBudget);
        verify(budgetRepository).save(testBudget);
    }

    @Test
    @DisplayName("deleteBudget() - should delete owned budget")
    void deleteBudget_ShouldDeleteBudget() {
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(testBudget));

        budgetService.deleteBudget(userId, budgetId);

        verify(budgetRepository).delete(testBudget);
    }

    @Test
    @DisplayName("updateSpentAmount() - should update and check alerts")
    void updateSpentAmount_ShouldUpdateAndCheckAlerts() {
        BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setCategoryId(10L);
        updateRequest.setAmount(new BigDecimal("100.00"));

        when(budgetRepository.updateSpentAmount(userId, 10L, updateRequest.getAmount())).thenReturn(1);
        when(budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, 10L)).thenReturn(Optional.of(testBudget));

        budgetService.updateSpentAmount(updateRequest);

        verify(budgetRepository).updateSpentAmount(any(), any(), any());
    }

    @Test
    @DisplayName("updateSpentAmount() - alert exceeded")
    void updateSpentAmount_AlertExceeded() {
        testBudget.setSpentAmount(new BigDecimal("1100.00")); // Exceeds 1000.00 limit

        BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setCategoryId(10L);
        updateRequest.setAmount(new BigDecimal("100.00"));

        when(budgetRepository.updateSpentAmount(any(), any(), any())).thenReturn(1);
        when(budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, 10L)).thenReturn(Optional.of(testBudget));

        budgetService.updateSpentAmount(updateRequest);

        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("updateSpentAmount() - threshold warning")
    void updateSpentAmount_ThresholdWarning() {
        testBudget.setSpentAmount(new BigDecimal("850.00")); // Above 80% threshold of 1000.00

        BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setCategoryId(10L);
        updateRequest.setAmount(new BigDecimal("100.00"));

        when(budgetRepository.updateSpentAmount(any(), any(), any())).thenReturn(1);
        when(budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, 10L)).thenReturn(Optional.of(testBudget));

        budgetService.updateSpentAmount(updateRequest);

        verify(rabbitTemplate).convertAndSend(eq(com.spendsmart.budget.config.RabbitMQConfig.NOTIFICATION_EXCHANGE), any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("updateSpentAmount() - refund should reduce spent amount and not alert")
    void updateSpentAmount_Refund_NoAlert() {
        testBudget.setSpentAmount(new BigDecimal("100.00")); // Reduced after refund

        BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setCategoryId(10L);
        updateRequest.setAmount(new BigDecimal("-50.00")); // Refund

        when(budgetRepository.updateSpentAmount(userId, 10L, new BigDecimal("-50.00"))).thenReturn(1);
        when(budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, 10L)).thenReturn(Optional.of(testBudget));

        budgetService.updateSpentAmount(updateRequest);

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("updateSpentAmount() - should do nothing when no active budget is updated")
    void updateSpentAmount_NoActiveBudget_ShouldSkipAlerts() {
        BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setCategoryId(10L);
        updateRequest.setAmount(new BigDecimal("15.00"));

        when(budgetRepository.updateSpentAmount(userId, 10L, updateRequest.getAmount())).thenReturn(0);

        budgetService.updateSpentAmount(updateRequest);

        verify(budgetRepository, never()).findByUserIdAndCategoryIdAndIsActiveTrue(userId, 10L);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("updateSpentAmount() - should swallow notification publishing failures")
    void updateSpentAmount_NotificationFailure_ShouldNotThrow() {
        testBudget.setSpentAmount(new BigDecimal("1000.00"));

        BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setCategoryId(10L);
        updateRequest.setAmount(new BigDecimal("25.00"));

        when(budgetRepository.updateSpentAmount(any(), any(), any())).thenReturn(1);
        when(budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, 10L)).thenReturn(Optional.of(testBudget));
        doThrow(new RuntimeException("RabbitMQ down")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), isA(Object.class));

        budgetService.updateSpentAmount(updateRequest);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), isA(Object.class));
    }

    @Test
    @DisplayName("resetExpiredBudgets() - should deactivate and save expired budgets")
    void resetExpiredBudgets_ShouldDeactivateExpiredBudgets() {
        Budget expiredBudget = Budget.builder()
                .budgetId(99L)
                .userId(userId)
                .categoryId(20L)
                .name("Expired")
                .limitAmount(new BigDecimal("500.00"))
                .spentAmount(new BigDecimal("200.00"))
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().minusDays(1))
                .isActive(true)
                .build();
        when(budgetRepository.findExpiredBudgets(any(LocalDate.class))).thenReturn(List.of(expiredBudget));

        budgetService.resetExpiredBudgets();

        assertThat(expiredBudget.getIsActive()).isFalse();
        verify(budgetRepository).save(expiredBudget);
    }

    @Test
    @DisplayName("getTotalBudgetByMonth() - should delegate to repository")
    void getTotalBudgetByMonth_ShouldReturnRepositorySum() {
        when(budgetRepository.sumLimitAmountByUserId(userId)).thenReturn(new BigDecimal("1234.56"));

        BigDecimal result = budgetService.getTotalBudgetByMonth(userId, 2026, 5);

        assertThat(result).isEqualByComparingTo("1234.56");
    }
    @Test
    @DisplayName("updateSpentAmount() - zero amount should do nothing")
    void updateSpentAmount_ZeroAmount_ShouldDoNothing() {
        BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
        updateRequest.setAmount(BigDecimal.ZERO);

        budgetService.updateSpentAmount(updateRequest);

        verifyNoInteractions(budgetRepository);
    }

    @Test
    @DisplayName("checkAlerts() - zero limit should not alert")
    void checkAlerts_ZeroLimit_NoAlert() {
        testBudget.setLimitAmount(BigDecimal.ZERO);
        testBudget.setSpentAmount(new BigDecimal("100.00"));

        BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setCategoryId(10L);
        updateRequest.setAmount(new BigDecimal("10.00"));

        when(budgetRepository.updateSpentAmount(any(), any(), any())).thenReturn(1);
        when(budgetRepository.findByUserIdAndCategoryIdAndIsActiveTrue(userId, 10L)).thenReturn(Optional.of(testBudget));

        budgetService.updateSpentAmount(updateRequest);

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("getBudgetById() - should throw exception when not owner")
    void getBudgetById_NotOwner_ShouldThrowException() {
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> budgetService.getBudgetById(99L, budgetId))
                .isInstanceOf(BudgetNotFoundException.class);
    }
}
