package com.spendsmart.auth.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for transactions viewed from the admin dashboard.
 * Aggregates core transaction data for platform monitoring.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private String id;
    private Long userId;
    private String userEmail;
    private String type; // EXPENSE or INCOME
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDateTime date;
}
