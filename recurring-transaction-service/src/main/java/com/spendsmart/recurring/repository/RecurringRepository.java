package com.spendsmart.recurring.repository;

import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserId(Long userId);

    List<RecurringTransaction> findByUserIdAndType(Long userId, TransactionType type);

    List<RecurringTransaction> findByUserIdAndIsActive(Long userId, Boolean isActive);

    List<RecurringTransaction> findByIsActiveAndNextDueDateBeforeOrNextDueDateEquals(Boolean isActive, LocalDate date, LocalDate date2);
    
    // Simpler query for scheduler
    List<RecurringTransaction> findByIsActiveTrueAndNextDueDateLessThanEqual(LocalDate date);

    Optional<RecurringTransaction> findByRecurringId(Long recurringId);

    List<RecurringTransaction> findByFrequency(Frequency frequency);

    long countByUserIdAndIsActive(Long userId, Boolean isActive);
}
