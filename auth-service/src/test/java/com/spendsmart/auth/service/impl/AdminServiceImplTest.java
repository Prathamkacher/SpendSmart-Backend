package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.client.ExpenseClient;
import com.spendsmart.auth.client.IncomeClient;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.dto.admin.PlatformAnalytics;
import com.spendsmart.auth.dto.admin.TransactionDTO;
import com.spendsmart.auth.entity.User;
import com.spendsmart.shared.exception.ResourceNotFoundException;
import com.spendsmart.auth.mapper.UserMapper;
import com.spendsmart.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private ExpenseClient expenseClient;
    @Mock private IncomeClient incomeClient;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User testUser;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .fullName("Test User")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllUsers_ShouldWork() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        when(userMapper.toProfileResponse(any())).thenReturn(new UserProfileResponse());
        
        List<UserProfileResponse> users = adminService.getAllUsers();
        assertThat(users).hasSize(1);
    }

    @Test
    void suspendUser_ShouldWork() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        adminService.suspendUser(userId);
        assertThat(testUser.getIsActive()).isFalse();
        verify(userRepository).save(testUser);
    }

    @Test
    void activateUser_ShouldWork() {
        testUser.setIsActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        adminService.activateUser(userId);
        assertThat(testUser.getIsActive()).isTrue();
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_ShouldWork() {
        when(userRepository.existsById(userId)).thenReturn(true);
        adminService.deleteUser(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_NotFound_ShouldThrow() {
        when(userRepository.existsById(userId)).thenReturn(false);
        assertThatThrownBy(() -> adminService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllTransactions_ShouldWork() {
        Map<String, Object> expense = new HashMap<>();
        expense.put("expenseId", "exp1");
        expense.put("userId", userId);
        expense.put("amount", "100.0");
        expense.put("date", "2026-05-01T10:00:00");

        Page<Map<String, Object>> expensePage = new PageImpl<>(Collections.singletonList(expense));
        when(expenseClient.getAllExpenses(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(ApiResponse.success("success", expensePage));
        when(incomeClient.getAllIncomes(anyInt(), anyInt(), any()))
                .thenReturn(ApiResponse.success("success", new PageImpl<>(Collections.emptyList())));

        List<TransactionDTO> transactions = adminService.getAllTransactions();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getId()).isEqualTo("exp1");
    }

    @Test
    void getPlatformAnalytics_ShouldWork() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        
        Map<String, Object> eStats = new HashMap<>();
        eStats.put("totalAmount", "5000.0");
        eStats.put("totalCount", "50");
        when(expenseClient.getGlobalStats()).thenReturn(ApiResponse.success("ok", eStats));
        
        Map<String, Object> iStats = new HashMap<>();
        iStats.put("totalAmount", "8000.0");
        iStats.put("totalCount", "40");
        when(incomeClient.getGlobalStats()).thenReturn(ApiResponse.success("ok", iStats));

        PlatformAnalytics analytics = adminService.getPlatformAnalytics();
        
        assertThat(analytics.getTotalUsers()).isEqualTo(10L);
        assertThat(analytics.getTotalExpenses()).isEqualByComparingTo("5000.0");
        assertThat(analytics.getTotalIncome()).isEqualByComparingTo("8000.0");
        assertThat(analytics.getTotalTransactions()).isEqualTo(90L);
    }

    @Test
    void sendGlobalNotification_ShouldWork() {
        adminService.sendGlobalNotification("Title", "Message", "HIGH");
        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    @Test
    void exportPlatformReport_ShouldWork() {
        lenient().when(userRepository.count()).thenReturn(1L);
        lenient().when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        lenient().when(expenseClient.getGlobalStats()).thenReturn(ApiResponse.success("ok", new HashMap<>()));
        lenient().when(incomeClient.getGlobalStats()).thenReturn(ApiResponse.success("ok", new HashMap<>()));

        byte[] report = adminService.exportPlatformReport();
        assertThat(report).isNotEmpty();
    }
    @Test
    void getAllTransactions_ServiceFailure_ShouldReturnEmptyAndNotThrow() {
        when(expenseClient.getAllExpenses(anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service Down"));

        List<TransactionDTO> transactions = adminService.getAllTransactions();
        assertThat(transactions).isEmpty();
    }

    @Test
    void getPlatformAnalytics_ServiceFailure_ShouldUseZeroValues() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        when(expenseClient.getGlobalStats()).thenThrow(new RuntimeException("Service Down"));

        PlatformAnalytics analytics = adminService.getPlatformAnalytics();
        assertThat(analytics.getTotalExpenses()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void mapToTransaction_SourceFallback_ShouldWork() {
        Map<String, Object> income = new HashMap<>();
        income.put("incomeId", "inc1");
        income.put("userId", userId);
        income.put("amount", "200.0");
        income.put("date", "2026-05-01"); // Simple date format
        income.put("source", "Salary");
        income.put("description", "Monthly Salary");

        Page<Map<String, Object>> incomePage = new PageImpl<>(Collections.singletonList(income));
        when(expenseClient.getAllExpenses(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(ApiResponse.success("success", new PageImpl<>(Collections.emptyList())));
        when(incomeClient.getAllIncomes(anyInt(), anyInt(), any()))
                .thenReturn(ApiResponse.success("success", incomePage));

        List<TransactionDTO> transactions = adminService.getAllTransactions();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getCategory()).isEqualTo("Salary");
        assertThat(transactions.get(0).getDescription()).isEqualTo("Monthly Salary");
    }

    @Test
    void parseFlexibleDate_Invalid_ShouldReturnNow() {
        Map<String, Object> expense = new HashMap<>();
        expense.put("expenseId", "exp1");
        expense.put("userId", userId);
        expense.put("amount", "100.0");
        expense.put("date", "invalid-date");

        Page<Map<String, Object>> expensePage = new PageImpl<>(Collections.singletonList(expense));
        when(expenseClient.getAllExpenses(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(ApiResponse.success("success", expensePage));
        when(incomeClient.getAllIncomes(anyInt(), anyInt(), any()))
                .thenReturn(ApiResponse.success("success", new PageImpl<>(Collections.emptyList())));

        List<TransactionDTO> transactions = adminService.getAllTransactions();
        assertThat(transactions.get(0).getDate()).isNotNull();
    }
}
