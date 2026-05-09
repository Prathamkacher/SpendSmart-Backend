package com.spendsmart.category.dto;

import com.spendsmart.category.entity.CategoryType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;

    @Size(max = 50, message = "Icon must be at most 50 characters")
    private String icon;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color code must be a valid HEX (e.g. #FF5733)")
    private String colorCode;

    @DecimalMin(value = "0.0", message = "Budget limit must be >= 0")
    private BigDecimal budgetLimit;
}
