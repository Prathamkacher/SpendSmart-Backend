package com.spendsmart.income.repository;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.IncomeSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Income} entities.
 * Supports paginated lookups and aggregation of user earnings.
 */
@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    Optional<Income> findByIncomeId(Long incomeId);

    Page<Income> findByUserId(Long userId, Pageable pageable);

    Page<Income> findByUserIdAndSource(Long userId, IncomeSource source, Pageable pageable);

    Page<Income> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT e FROM Income e WHERE e.userId = :userId " +
           "AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    Page<Income> findByUserIdAndMonth(@Param("userId") Long userId,
                                      @Param("year") int year,
                                      @Param("month") int month,
                                      Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Income e WHERE e.userId = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Income e " +
           "WHERE e.userId = :userId AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal sumAmountByUserIdAndMonth(@Param("userId") Long userId,
                                         @Param("year") int year,
                                         @Param("month") int month);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Income e")
    BigDecimal sumAllIncome();

    List<Income> findByIsRecurringTrue();

    void deleteByIncomeId(Long incomeId);
}
