package com.spendsmart.income.dto;

import com.spendsmart.income.entity.IncomeSource;
import com.spendsmart.income.entity.RecurrencePeriod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for income information.
 * Returns detailed record of a user's income transaction.
 */
@Data
public class IncomeResponse {
    private Long incomeId;
    private Long userId;
    private Long categoryId;
    private String title;
    private BigDecimal amount;
    private String currency;
    private IncomeSource source;
    private LocalDate date;
    private String notes;
    private Boolean isRecurring;
    private RecurrencePeriod recurrencePeriod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
