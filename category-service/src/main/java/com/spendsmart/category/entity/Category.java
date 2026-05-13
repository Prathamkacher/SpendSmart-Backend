package com.spendsmart.category.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a category in the system.
 * Categories can be user-defined or system defaults, and are used to group transactions.
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    /**
     * Unique identifier for the category.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * ID of the user who owns this category.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Name of the category (e.g., "Food", "Salary").
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Type of category: INCOME or EXPENSE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CategoryType type;

    /**
     * Icon representation (emoji or string identifier).
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * HEX color code for UI display.
     */
    @Column(name = "color_code", length = 7)
    @Builder.Default
    private String colorCode = "#6366F1";

    /**
     * Optional monthly budget limit for this category.
     */
    @Column(name = "budget_limit", precision = 15, scale = 2)
    private BigDecimal budgetLimit;

    /**
     * Flag indicating if this is a global default category.
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Timestamp when the category was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the category was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
