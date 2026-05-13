package com.spendsmart.income.service.impl;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.dto.IncomeResponse;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.IncomeSource;
import com.spendsmart.income.exception.IncomeNotFoundException;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import com.spendsmart.income.mapper.IncomeMapper;
import com.spendsmart.income.repository.IncomeRepository;
import com.spendsmart.income.service.IncomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link IncomeService}.
 * Manages the persistence and retrieval of user income records.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;

    @Override
    @Transactional
    public IncomeResponse addIncome(Long userId, IncomeRequest request) {
        log.info("Adding new income for userId: {}", userId);
        Income income = incomeMapper.toEntity(request);
        income.setUserId(userId);
        
        Income savedIncome = incomeRepository.save(income);
        return incomeMapper.toResponse(savedIncome);
    }

    @Override
    public IncomeResponse getIncomeById(Long userId, Long incomeId) {
        Income income = incomeRepository.findByIncomeId(incomeId)
                .orElseThrow(() -> new IncomeNotFoundException("Income not found with id: " + incomeId));
        
        if (!income.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have permission to access this income");
        }
        
        return incomeMapper.toResponse(income);
    }

    @Override
    public Page<IncomeResponse> getIncomesByUser(Long userId, Pageable pageable) {
        return incomeRepository.findByUserId(userId, pageable)
                .map(incomeMapper::toResponse);
    }

    @Override
    public Page<IncomeResponse> getIncomesBySource(Long userId, IncomeSource source, Pageable pageable) {
        return incomeRepository.findByUserIdAndSource(userId, source, pageable)
                .map(incomeMapper::toResponse);
    }

    @Override
    public Page<IncomeResponse> getIncomesByDateRange(Long userId, LocalDate start, LocalDate end, Pageable pageable) {
        return incomeRepository.findByUserIdAndDateBetween(userId, start, end, pageable)
                .map(incomeMapper::toResponse);
    }

    @Override
    public Page<IncomeResponse> getIncomesByMonth(Long userId, int year, int month, Pageable pageable) {
        return incomeRepository.findByUserIdAndMonth(userId, year, month, pageable)
                .map(incomeMapper::toResponse);
    }

    @Override
    @Transactional
    public IncomeResponse updateIncome(Long userId, Long incomeId, IncomeRequest request) {
        Income income = incomeRepository.findByIncomeId(incomeId)
                .orElseThrow(() -> new IncomeNotFoundException("Income not found with id: " + incomeId));

        if (!income.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have permission to update this income");
        }

        incomeMapper.updateEntity(income, request);
        Income updatedIncome = incomeRepository.save(income);
        return incomeMapper.toResponse(updatedIncome);
    }

    @Override
    @Transactional
    public void deleteIncome(Long userId, Long incomeId) {
        Income income = incomeRepository.findByIncomeId(incomeId)
                .orElseThrow(() -> new IncomeNotFoundException("Income not found with id: " + incomeId));

        if (!income.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have permission to delete this income");
        }

        incomeRepository.delete(income);
    }

    @Override
    public BigDecimal getTotalIncomeByUser(Long userId) {
        BigDecimal total = incomeRepository.sumAmountByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalIncomeByMonth(Long userId, int year, int month) {
        BigDecimal total = incomeRepository.sumAmountByUserIdAndMonth(userId, year, month);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<IncomeResponse> getRecurringIncomes() {
        return incomeRepository.findByIsRecurringTrue()
                .stream()
                .map(incomeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<IncomeResponse> getAllIncomes(Pageable pageable) {
        log.info("Admin: Fetching all platform incomes");
        return incomeRepository.findAll(pageable)
                .map(incomeMapper::toResponse);
    }

    @Override
    public BigDecimal getGlobalTotalIncome() {
        log.info("Admin: Calculating global total income");
        BigDecimal total = incomeRepository.sumAllIncome();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public long getGlobalIncomeCount() {
        log.info("Admin: Counting global incomes");
        return incomeRepository.count();
    }
}
