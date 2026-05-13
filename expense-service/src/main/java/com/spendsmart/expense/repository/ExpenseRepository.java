package com.spendsmart.expense.repository;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for managing {@link Expense} entities.
 * Supports paginated queries, complex aggregations for analytics, and full-text keyword searches.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ── Lookups ──────────────────────────────────────────────────────

    Optional<Expense> findByExpenseId(Long expenseId);

    // ── Paginated finders ────────────────────────────────────────────

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    Page<Expense> findByUserIdAndType(Long userId, ExpenseType type, Pageable pageable);

    Page<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    Page<Expense> findByUserIdAndDate(Long userId, LocalDate date, Pageable pageable);

    Page<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // ── Aggregations ─────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId AND e.categoryId = :categoryId")
    BigDecimal sumAmountByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.userId = :userId AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal sumAmountByUserIdAndMonth(@Param("userId") Long userId,
                                        @Param("year") int year,
                                        @Param("month") int month);

    @Query("SELECT e.categoryId, COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.userId = :userId AND YEAR(e.date) = :year AND MONTH(e.date) = :month " +
           "GROUP BY e.categoryId")
    java.util.List<Object[]> sumAmountByUserIdAndMonthGroupByCategory(@Param("userId") Long userId,
                                                                       @Param("year") int year,
                                                                       @Param("month") int month);

    @Query("SELECT e.date, COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.userId = :userId AND YEAR(e.date) = :year AND MONTH(e.date) = :month " +
           "GROUP BY e.date")
    java.util.List<Object[]> sumAmountByUserIdAndMonthGroupByDate(@Param("userId") Long userId,
                                                                   @Param("year") int year,
                                                                   @Param("month") int month);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAllExpenses();

    // ── Search ───────────────────────────────────────────────────────

    @Query("SELECT e FROM Expense e WHERE e.userId = :userId " +
           "AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR LOWER(e.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Expense> searchByKeyword(@Param("userId") Long userId,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    // ── Month filter ─────────────────────────────────────────────────

    @Query("SELECT e FROM Expense e WHERE e.userId = :userId " +
           "AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    Page<Expense> findByUserIdAndMonth(@Param("userId") Long userId,
                                       @Param("year") int year,
                                       @Param("month") int month,
                                       Pageable pageable);

    // ── Delete ───────────────────────────────────────────────────────

    void deleteByExpenseId(Long expenseId);
}
