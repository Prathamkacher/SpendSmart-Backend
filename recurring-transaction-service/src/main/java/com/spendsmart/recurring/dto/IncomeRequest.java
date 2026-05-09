package com.spendsmart.recurring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeRequest {
    private Long categoryId;
    private String title;
    private BigDecimal amount;
    private String currency; // "INR"
    private String source; // Should be name of IncomeSource enum
    private LocalDate date;
    private String notes;
    private Boolean isRecurring;
}
