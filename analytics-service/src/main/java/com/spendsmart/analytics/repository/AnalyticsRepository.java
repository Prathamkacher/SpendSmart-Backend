package com.spendsmart.analytics.repository;

import com.spendsmart.analytics.entity.FinancialSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link FinancialSnapshot} entities.
 * Provides custom queries for calculating historical trends and averages.
 */
@Repository
public interface AnalyticsRepository extends JpaRepository<FinancialSnapshot, Long> {

    List<FinancialSnapshot> findByUserId(Long userId);

    Optional<FinancialSnapshot> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);

    List<FinancialSnapshot> findByUserIdAndYear(Long userId, Integer year);

    @Query("SELECT AVG(s.savingsRate) FROM FinancialSnapshot s WHERE s.userId = :userId")
    Double avgSavingsRateByUserId(Long userId);

    @Query("SELECT s.topCategory, SUM(s.totalExpenses) FROM FinancialSnapshot s WHERE s.userId = :userId GROUP BY s.topCategory")
    List<Object[]> sumExpensesByCategory(Long userId);

    @Query("SELECT s FROM FinancialSnapshot s WHERE s.userId = :userId ORDER BY s.totalExpenses DESC")
    List<FinancialSnapshot> findTopSpendingMonths(Long userId);

    long countByUserId(Long userId);
}
