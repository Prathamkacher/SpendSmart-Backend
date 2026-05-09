package com.spendsmart.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snapshotId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String period; // e.g., "MONTHLY"

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private BigDecimal savingsRate;
    private String topCategory;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
