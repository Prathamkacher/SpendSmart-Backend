package com.spendsmart.recurring.service;

import com.spendsmart.recurring.client.ExpenseServiceClient;
import com.spendsmart.recurring.client.IncomeServiceClient;
import com.spendsmart.recurring.client.NotificationServiceClient;
import com.spendsmart.recurring.dto.ExpenseRequest;
import com.spendsmart.recurring.dto.IncomeRequest;
import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.entity.TransactionType;
import com.spendsmart.recurring.repository.RecurringRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecurringServiceTest {

    @Mock
    private RecurringRepository recurringRepository;

    @Mock
    private ExpenseServiceClient expenseServiceClient;

    @Mock
    private IncomeServiceClient incomeServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private RecurringServiceImpl recurringService;

    private RecurringTransaction mockExpense;
    private RecurringTransaction mockIncome;

    @BeforeEach
    void setUp() {
        mockExpense = RecurringTransaction.builder()
                .recurringId(1L)
                .userId(100L)
                .categoryId(10L)
                .title("Netflix Subscription")
                .amount(BigDecimal.valueOf(15.99))
                .type(TransactionType.EXPENSE)
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.now().minusMonths(1))
                .nextDueDate(LocalDate.now())
                .isActive(true)
                .build();

        mockIncome = RecurringTransaction.builder()
                .recurringId(2L)
                .userId(100L)
                .categoryId(20L)
                .title("Salary")
                .amount(BigDecimal.valueOf(5000))
                .type(TransactionType.INCOME)
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.now().minusMonths(1))
                .nextDueDate(LocalDate.now())
                .isActive(true)
                .build();

        ReflectionTestUtils.setField(recurringService, "jwtSecret", "ThisIsAVerySecretKeyForTestingPurposes1234567890");
    }

    @Test
    void testProcessUpcomingDue_Expense() {
        // Arrange
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(
                eq(true), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(mockExpense));

        when(recurringRepository.findById(1L)).thenReturn(Optional.of(mockExpense));

        // Act
        recurringService.processUpcomingDue();

        // Assert
        verify(expenseServiceClient, times(1)).createExpense(any(ExpenseRequest.class), anyString(), anyLong());
        verify(recurringRepository, times(1)).save(mockExpense);
        
        // Assert next due date was updated
        assertEquals(LocalDate.now().plusMonths(1), mockExpense.getNextDueDate());
    }

    @Test
    void testProcessUpcomingDue_Income() {
        // Arrange
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(
                eq(true), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(mockIncome));

        when(recurringRepository.findById(2L)).thenReturn(Optional.of(mockIncome));

        // Act
        recurringService.processUpcomingDue();

        // Assert
        verify(incomeServiceClient, times(1)).createIncome(any(IncomeRequest.class), anyString(), anyLong());
        verify(recurringRepository, times(1)).save(mockIncome);
        
        // Assert next due date was updated
        assertEquals(LocalDate.now().plusMonths(1), mockIncome.getNextDueDate());
    }

    @Test
    void testUpdateNextDueDate_AllFrequencies() {
        LocalDate current = LocalDate.of(2026, 1, 1);
        
        assertEquals(LocalDate.of(2026, 1, 2), recurringService.calculateNextDueDate(current, Frequency.DAILY));
        assertEquals(LocalDate.of(2026, 1, 8), recurringService.calculateNextDueDate(current, Frequency.WEEKLY));
        assertEquals(LocalDate.of(2026, 2, 1), recurringService.calculateNextDueDate(current, Frequency.MONTHLY));
        assertEquals(LocalDate.of(2027, 1, 1), recurringService.calculateNextDueDate(current, Frequency.YEARLY));
    }

    @Test
    void testProcessUpcomingDue_EndDatePassed() {
        // Arrange
        mockExpense.setEndDate(LocalDate.now().minusDays(1)); // Ended yesterday
        
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(
                eq(true), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(mockExpense));

        // Act
        recurringService.processUpcomingDue();

        // Assert
        assertFalse(mockExpense.getIsActive());
        verify(expenseServiceClient, never()).createExpense(any(), anyString(), anyLong());
        verify(recurringRepository, times(1)).save(mockExpense);
    }
}
