package com.spendsmart.budget.repository;

import com.spendsmart.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Budget} entities.
 * Supports active budget lookups and atomic spending updates.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Budget> findByUserIdAndCategoryIdAndIsActiveTrue(Long userId, Long categoryId);

    @Modifying
    @Query("UPDATE Budget b SET b.spentAmount = b.spentAmount + :amountDelta " +
           "WHERE b.userId = :userId AND b.categoryId = :categoryId AND b.isActive = true")
    int updateSpentAmount(@Param("userId") Long userId, 
                          @Param("categoryId") Long categoryId, 
                          @Param("amountDelta") BigDecimal amountDelta);

    @Query("SELECT b FROM Budget b WHERE b.isActive = true AND b.endDate < :currentDate")
    List<Budget> findExpiredBudgets(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT COALESCE(SUM(b.limitAmount), 0) FROM Budget b WHERE b.userId = :userId AND b.isActive = true")
    BigDecimal sumLimitAmountByUserId(@Param("userId") Long userId);
}
