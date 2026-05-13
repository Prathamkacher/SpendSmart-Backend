package com.spendsmart.income.service;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.dto.IncomeResponse;
import com.spendsmart.income.entity.IncomeSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for income management.
 * Defines core operations for tracking user earnings and generating financial summaries.
 */
public interface IncomeService {

    IncomeResponse addIncome(Long userId, IncomeRequest request);

    IncomeResponse getIncomeById(Long userId, Long incomeId);

    Page<IncomeResponse> getIncomesByUser(Long userId, Pageable pageable);

    Page<IncomeResponse> getIncomesBySource(Long userId, IncomeSource source, Pageable pageable);

    Page<IncomeResponse> getIncomesByDateRange(Long userId, LocalDate start, LocalDate end, Pageable pageable);

    Page<IncomeResponse> getIncomesByMonth(Long userId, int year, int month, Pageable pageable);

    IncomeResponse updateIncome(Long userId, Long incomeId, IncomeRequest request);

    void deleteIncome(Long userId, Long incomeId);

    BigDecimal getTotalIncomeByUser(Long userId);

    BigDecimal getTotalIncomeByMonth(Long userId, int year, int month);

    List<IncomeResponse> getRecurringIncomes();

    // Admin methods
    Page<IncomeResponse> getAllIncomes(Pageable pageable);

    BigDecimal getGlobalTotalIncome();

    long getGlobalIncomeCount();
}
