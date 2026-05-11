package com.spendsmart.recurring.service;

import com.spendsmart.recurring.client.ExpenseServiceClient;
import com.spendsmart.recurring.client.IncomeServiceClient;
import com.spendsmart.recurring.config.RabbitMQConfig;
import com.spendsmart.recurring.dto.*;
import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.entity.TransactionType;
import com.spendsmart.shared.exception.ResourceNotFoundException;
import com.spendsmart.recurring.repository.RecurringRepository;
import com.spendsmart.shared.events.NotificationEvent;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecurringServiceImpl implements RecurringService {

    private final RecurringRepository recurringRepository;
    private final ExpenseServiceClient expenseServiceClient;
    private final IncomeServiceClient incomeServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    @Transactional
    public RecurringResponse addRecurring(Long userId, RecurringRequest request) {
        log.info("Adding recurring transaction for userId={}, title='{}'", userId, request.getTitle());
        RecurringTransaction recurring = RecurringTransaction.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .amount(request.getAmount())
                .type(request.getType())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextDueDate(request.isSkipFirstGeneration() 
                    ? calculateNextDueDate(request.getStartDate(), request.getFrequency()) 
                    : request.getStartDate()) 
                .isActive(true)
                .description(request.getDescription())
                .incomeSource(request.getIncomeSource())
                .paymentMethod(request.getPaymentMethod())
                .build();

        RecurringTransaction saved = recurringRepository.save(recurring);
        if (processTransaction(saved)) {
            saved = recurringRepository.save(saved);
        }
        return mapToResponse(saved);
    }

    @Override
    public List<RecurringResponse> getByUser(Long userId) {
        return recurringRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RecurringResponse getById(Long recurringId) {
        RecurringTransaction recurring = recurringRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        return mapToResponse(recurring);
    }

    @Override
    public List<RecurringResponse> getActiveRecurring(Long userId) {
        return recurringRepository.findByUserIdAndIsActive(userId, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecurringResponse updateRecurring(Long recurringId, Long userId, RecurringRequest request) {
        RecurringTransaction recurring = recurringRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (!recurring.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to modify this record");
        }

        boolean startDateChanged = !recurring.getStartDate().equals(request.getStartDate());
        boolean frequencyChanged = !recurring.getFrequency().equals(request.getFrequency());

        recurring.setCategoryId(request.getCategoryId());
        recurring.setTitle(request.getTitle());
        recurring.setAmount(request.getAmount());
        recurring.setType(request.getType());
        recurring.setFrequency(request.getFrequency());
        recurring.setStartDate(request.getStartDate());
        recurring.setEndDate(request.getEndDate());
        recurring.setDescription(request.getDescription());
        recurring.setPaymentMethod(request.getPaymentMethod());

        if (startDateChanged || frequencyChanged) {
            log.info("Resetting nextDueDate for ID {} because start date or frequency changed", recurringId);
            recurring.setNextDueDate(request.getStartDate());
        }
        
        RecurringTransaction updated = recurringRepository.save(recurring);
        processTransaction(updated);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public RecurringResponse deactivateRecurring(Long recurringId, Long userId) {
        RecurringTransaction recurring = recurringRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (!recurring.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to modify this record");
        }

        recurring.setIsActive(false);
        RecurringTransaction updated = recurringRepository.save(recurring);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public RecurringResponse activateRecurring(Long recurringId, Long userId) {
        RecurringTransaction recurring = recurringRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (!recurring.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to modify this record");
        }

        recurring.setIsActive(true);
        // If the next due date was in the past while it was paused, 
        // we should probably catch up or at least set it to a future date.
        // For now, let's process it which will catch up any missed ones.
        processTransaction(recurring);
        RecurringTransaction updated = recurringRepository.save(recurring);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRecurring(Long recurringId, Long userId) {
        RecurringTransaction recurring = recurringRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (!recurring.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to modify this record");
        }

        recurringRepository.delete(recurring);
    }

    @Override
    @Transactional
    public void processUpcomingDue() {
        LocalDate today = LocalDate.now();
        log.info("Starting recurring transaction processing for {}", today);
        
        // 1. Send Notifications 3 days in advance
        LocalDate threeDaysFromNow = today.plusDays(3);
        List<RecurringTransaction> approaching = recurringRepository.findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(true, threeDaysFromNow, threeDaysFromNow);
        for (RecurringTransaction t : approaching) {
            if (t.getNextDueDate() != null && t.getNextDueDate().equals(threeDaysFromNow)) {
                sendReminder(t);
            }
        }

        // 2. Process Due Transactions
        List<RecurringTransaction> dueTransactions = recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(today);
        log.info("Found {} due recurring transactions", dueTransactions.size());
        
        for (RecurringTransaction t : dueTransactions) {
            if (processTransaction(t)) {
                recurringRepository.save(t);
            }
        }
    }

    private boolean processTransaction(RecurringTransaction t) {
        LocalDate today = LocalDate.now();
        try {
            boolean changed = false;
            // Generate transactions until nextDueDate is in the future
            while (t.getNextDueDate() != null && !t.getNextDueDate().isAfter(today)) {
                log.info("Processing due recurring transaction ID: {} ('{}') for date: {}", 
                    t.getRecurringId(), t.getTitle(), t.getNextDueDate());
                
                if (t.getEndDate() != null && t.getNextDueDate().isAfter(t.getEndDate())) {
                    log.info("Deactivating expired recurring transaction ID: {}", t.getRecurringId());
                    t.setIsActive(false);
                    changed = true;
                    break;
                }

                try {
                    generateTransactionFromRecurring(t.getRecurringId(), t.getNextDueDate());
                    log.info("Successfully generated transaction for ID: {} on date: {}", t.getRecurringId(), t.getNextDueDate());
                } catch (Exception e) {
                    log.error("Failed to generate transaction for ID: {} on date: {}: {}. Breaking loop to prevent incorrect date advancement.", 
                        t.getRecurringId(), t.getNextDueDate(), e.getMessage());
                    // If generation fails, we break the loop so we can try again for the SAME date later
                    break;
                }
                
                // Update next due date
                LocalDate nextDate = calculateNextDueDate(t.getNextDueDate(), t.getFrequency());
                log.info("Advancing next due date for ID {}: {} -> {}", t.getRecurringId(), t.getNextDueDate(), nextDate);
                t.setNextDueDate(nextDate);
                changed = true;
            }
            return changed;
        } catch (Exception e) {
            log.error("Failed to process recurring transaction ID {}: {}. Will retry in next run.", t.getRecurringId(), e.getMessage());
            return false;
        }
    }

    @Override
    public void generateTransactionFromRecurring(Long recurringId) {
        generateTransactionFromRecurring(recurringId, LocalDate.now());
    }

    private void generateTransactionFromRecurring(Long recurringId, LocalDate date) {
        RecurringTransaction t = recurringRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring not found"));

        String internalToken = "Bearer " + generateInternalToken(t.getUserId());

        if (t.getType() == TransactionType.EXPENSE) {
            ExpenseRequest req = ExpenseRequest.builder()
                    .categoryId(t.getCategoryId())
                    .title(t.getTitle() + " (Auto-Recurring)")
                    .amount(t.getAmount())
                    .currency("INR")
                    .type(t.getType().name())
                    .paymentMethod(t.getPaymentMethod())
                    .date(date)
                    .notes("Generated by recurring service")
                    .isRecurring(true)
                    .build();
            expenseServiceClient.createExpense(req, internalToken, t.getUserId());
        } else if (t.getType() == TransactionType.INCOME) {
            IncomeRequest req = IncomeRequest.builder()
                    .categoryId(t.getCategoryId())
                    .title(t.getTitle() + " (Auto-Recurring)")
                    .amount(t.getAmount())
                    .currency("INR")
                    .source(t.getIncomeSource() != null ? t.getIncomeSource() : "OTHER")
                    .date(date)
                    .notes("Generated by recurring service")
                    .isRecurring(true)
                    .build();
            incomeServiceClient.createIncome(req, internalToken, t.getUserId());
        }
    }

    private String generateInternalToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", "SYSTEM")
                .subject("system@spendsmart.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();
    }

    private void sendReminder(RecurringTransaction t) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .recipientId(t.getUserId())
                    .type("RECURRING_DUE")
                    .severity("INFO")
                    .title("Upcoming " + t.getType())
                    .message("Your recurring " + t.getType() + " '" + t.getTitle() + "' of amount " + t.getAmount() + " is due in 3 days.")
                    .relatedId(t.getRecurringId())
                    .relatedType("RECURRING")
                    .build();
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY, event);
            log.info("Sent recurring reminder event to RabbitMQ for user {}", t.getUserId());
        } catch (Exception e) {
            log.warn("Failed to send recurring reminder to RabbitMQ: {}", e.getMessage());
        }
    }

    public LocalDate calculateNextDueDate(LocalDate current, Frequency frequency) {
        switch (frequency) {
            case DAILY: return current.plusDays(1);
            case WEEKLY: return current.plusWeeks(1);
            case MONTHLY: return current.plusMonths(1);
            case YEARLY: return current.plusYears(1);
            default: return current.plusDays(1);
        }
    }

    private RecurringResponse mapToResponse(RecurringTransaction t) {
        return RecurringResponse.builder()
                .recurringId(t.getRecurringId())
                .userId(t.getUserId())
                .categoryId(t.getCategoryId())
                .title(t.getTitle())
                .amount(t.getAmount())
                .type(t.getType())
                .frequency(t.getFrequency())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .nextDueDate(t.getNextDueDate())
                .isActive(t.getIsActive())
                .description(t.getDescription())
                .paymentMethod(t.getPaymentMethod())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
