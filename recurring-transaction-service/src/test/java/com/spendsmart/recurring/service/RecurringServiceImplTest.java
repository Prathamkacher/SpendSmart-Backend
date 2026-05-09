package com.spendsmart.recurring.service;

import com.spendsmart.recurring.client.ExpenseServiceClient;
import com.spendsmart.recurring.client.IncomeServiceClient;
import com.spendsmart.recurring.dto.RecurringRequest;
import com.spendsmart.recurring.dto.RecurringResponse;
import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.entity.TransactionType;
import com.spendsmart.recurring.repository.RecurringRepository;
import com.spendsmart.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurringServiceImpl Unit Tests")
class RecurringServiceImplTest {

    @Mock private RecurringRepository recurringRepository;
    @Mock private ExpenseServiceClient expenseServiceClient;
    @Mock private IncomeServiceClient incomeServiceClient;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RecurringServiceImpl recurringService;

    private RecurringTransaction testTransaction;
    private RecurringRequest recurringRequest;
    private final Long userId = 1L;
    private final Long recurringId = 1L;

    @BeforeEach
    void setUp() {
        testTransaction = RecurringTransaction.builder()
                .recurringId(recurringId)
                .userId(userId)
                .title("Internet Bill")
                .amount(new BigDecimal("1000.00"))
                .type(TransactionType.EXPENSE)
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.now())
                .nextDueDate(LocalDate.now())
                .isActive(true)
                .build();

        recurringRequest = new RecurringRequest();
        recurringRequest.setTitle("Internet Bill");
        recurringRequest.setAmount(new BigDecimal("1000.00"));
        recurringRequest.setType(TransactionType.EXPENSE);
        recurringRequest.setFrequency(Frequency.MONTHLY);
        recurringRequest.setStartDate(LocalDate.now());

        ReflectionTestUtils.setField(recurringService, "jwtSecret", "ThisIsAVerySecretKeyForTestingPurposes1234567890");
    }

    @Test
    @DisplayName("addRecurring() - should save transaction")
    void addRecurring_ShouldSave() {
        when(recurringRepository.save(any())).thenReturn(testTransaction);

        RecurringResponse response = recurringService.addRecurring(userId, recurringRequest);

        assertThat(response).isNotNull();
        verify(recurringRepository).save(any());
    }

    @Test
    @DisplayName("processUpcomingDue() - should handle due transactions")
    void processUpcomingDue_ShouldHandleDue() {
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(anyBoolean(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any())).thenReturn(Collections.singletonList(testTransaction));
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        recurringService.processUpcomingDue();

        verify(expenseServiceClient).createExpense(any(), anyString(), anyLong());
        verify(recurringRepository).save(testTransaction);
        assertThat(testTransaction.getNextDueDate()).isEqualTo(LocalDate.now().plusMonths(1));
    }

    @Test
    @DisplayName("processUpcomingDue() - should handle income type")
    void processUpcomingDue_IncomeType_ShouldWork() {
        testTransaction.setType(TransactionType.INCOME);
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(anyBoolean(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any())).thenReturn(Collections.singletonList(testTransaction));
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        recurringService.processUpcomingDue();

        verify(incomeServiceClient).createIncome(any(), anyString(), anyLong());
        verify(recurringRepository).save(testTransaction);
    }

    @Test
    @DisplayName("processUpcomingDue() - should deactivate expired transactions")
    void processUpcomingDue_Expired_ShouldDeactivate() {
        testTransaction.setEndDate(LocalDate.now().minusDays(1));
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(anyBoolean(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any())).thenReturn(Collections.singletonList(testTransaction));

        recurringService.processUpcomingDue();

        assertThat(testTransaction.getIsActive()).isFalse();
        verify(recurringRepository).save(testTransaction);
        verify(expenseServiceClient, never()).createExpense(any(), anyString(), anyLong());
    }

    @Test
    @DisplayName("deleteRecurring() - should throw exception if unauthorized")
    void deleteRecurring_Unauthorized_ShouldThrowException() {
        testTransaction.setUserId(999L); // Different user
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        assertThatThrownBy(() -> recurringService.deleteRecurring(recurringId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    @DisplayName("getByUser() - should map repository results")
    void getByUser_ShouldReturnMappedResponses() {
        when(recurringRepository.findByUserId(userId)).thenReturn(List.of(testTransaction));

        List<RecurringResponse> responses = recurringService.getByUser(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Internet Bill");
    }

    @Test
    @DisplayName("getById() - should return mapped response")
    void getById_ShouldReturnResponse() {
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        RecurringResponse response = recurringService.getById(recurringId);

        assertThat(response.getRecurringId()).isEqualTo(recurringId);
    }

    @Test
    @DisplayName("getActiveRecurring() - should return active records")
    void getActiveRecurring_ShouldReturnResponses() {
        when(recurringRepository.findByUserIdAndIsActive(userId, true)).thenReturn(List.of(testTransaction));

        List<RecurringResponse> responses = recurringService.getActiveRecurring(userId);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("updateRecurring() - should update owned record")
    void updateRecurring_ShouldUpdateOwnedRecord() {
        recurringRequest.setDescription("Updated description");
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));
        when(recurringRepository.save(testTransaction)).thenReturn(testTransaction);

        RecurringResponse response = recurringService.updateRecurring(recurringId, userId, recurringRequest);

        assertThat(response.getDescription()).isEqualTo("Updated description");
        verify(recurringRepository).save(testTransaction);
    }

    @Test
    @DisplayName("updateRecurring() - should reject other users")
    void updateRecurring_ShouldRejectUnauthorizedUser() {
        testTransaction.setUserId(77L);
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        assertThatThrownBy(() -> recurringService.updateRecurring(recurringId, userId, recurringRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    @DisplayName("deactivateRecurring() - should deactivate owned record")
    void deactivateRecurring_ShouldDeactivateOwnedRecord() {
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));
        when(recurringRepository.save(testTransaction)).thenReturn(testTransaction);

        RecurringResponse response = recurringService.deactivateRecurring(recurringId, userId);

        assertThat(response.getIsActive()).isFalse();
        verify(recurringRepository).save(testTransaction);
    }

    @Test
    @DisplayName("deactivateRecurring() - should reject other users")
    void deactivateRecurring_ShouldRejectUnauthorizedUser() {
        testTransaction.setUserId(88L);
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        assertThatThrownBy(() -> recurringService.deactivateRecurring(recurringId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    @DisplayName("deleteRecurring() - should delete owned record")
    void deleteRecurring_ShouldDeleteOwnedRecord() {
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        recurringService.deleteRecurring(recurringId, userId);

        verify(recurringRepository).delete(testTransaction);
    }

    @Test
    @DisplayName("processUpcomingDue() - should send reminder for transactions due in three days")
    void processUpcomingDue_ShouldSendReminderForApproachingDueDate() {
        testTransaction.setNextDueDate(LocalDate.now().plusDays(3));
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(anyBoolean(), any(), any()))
                .thenReturn(List.of(testTransaction));
        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any())).thenReturn(Collections.emptyList());

        recurringService.processUpcomingDue();

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("processUpcomingDue() - should continue when transaction generation fails")
    void processUpcomingDue_ShouldSwallowGenerationFailures() {
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(anyBoolean(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any())).thenReturn(List.of(testTransaction));
        when(recurringRepository.findById(recurringId)).thenThrow(new RuntimeException("db down"));

        recurringService.processUpcomingDue();

        verify(recurringRepository, never()).save(testTransaction);
    }

    @Test
    @DisplayName("generateTransactionFromRecurring() - should create expense transaction")
    void generateTransactionFromRecurring_ShouldCreateExpense() {
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        recurringService.generateTransactionFromRecurring(recurringId);

        verify(expenseServiceClient).createExpense(any(), anyString(), eq(userId));
    }

    @Test
    @DisplayName("generateTransactionFromRecurring() - should create income transaction")
    void generateTransactionFromRecurring_ShouldCreateIncome() {
        testTransaction.setType(TransactionType.INCOME);
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(testTransaction));

        recurringService.generateTransactionFromRecurring(recurringId);

        verify(incomeServiceClient).createIncome(any(), anyString(), eq(userId));
    }

    @Test
    @DisplayName("calculateNextDueDate() - should support all frequencies")
    void calculateNextDueDate_ShouldSupportAllFrequencies() {
        LocalDate current = LocalDate.of(2026, 5, 4);

        assertThat(recurringService.calculateNextDueDate(current, Frequency.DAILY)).isEqualTo(current.plusDays(1));
        assertThat(recurringService.calculateNextDueDate(current, Frequency.WEEKLY)).isEqualTo(current.plusWeeks(1));
        assertThat(recurringService.calculateNextDueDate(current, Frequency.MONTHLY)).isEqualTo(current.plusMonths(1));
        assertThat(recurringService.calculateNextDueDate(current, Frequency.YEARLY)).isEqualTo(current.plusYears(1));
    }
    @Test
    @DisplayName("processUpcomingDue() - should handle reminder notification failures")
    void processUpcomingDue_ReminderFailure_ShouldNotThrow() {
        testTransaction.setNextDueDate(LocalDate.now().plusDays(3));
        when(recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(anyBoolean(), any(), any()))
                .thenReturn(List.of(testTransaction));
        when(recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(any())).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("RabbitMQ error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> recurringService.processUpcomingDue());
    }

    @Test
    @DisplayName("generateTransactionFromRecurring() - should throw exception if not found")
    void generateTransactionFromRecurring_NotFound_ShouldThrowException() {
        when(recurringRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recurringService.generateTransactionFromRecurring(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
