package com.spendsmart.category.dto;

import com.spendsmart.category.entity.CategoryType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating or updating a category.
 * Contains validation constraints for category details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    /**
     * Name of the category.
     */
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    /**
     * Type of category (INCOME or EXPENSE).
     */
    @NotNull(message = "Category type is required")
    private CategoryType type;

    /**
     * Icon representation for the category (emoji or icon name).
     */
    @Size(max = 50, message = "Icon must be at most 50 characters")
    private String icon;

    /**
     * HEX color code for the category UI.
     */
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color code must be a valid HEX (e.g. #FF5733)")
    private String colorCode;

    /**
     * Monthly budget limit for this category.
     */
    @DecimalMin(value = "0.0", message = "Budget limit must be >= 0")
    private BigDecimal budgetLimit;
}
