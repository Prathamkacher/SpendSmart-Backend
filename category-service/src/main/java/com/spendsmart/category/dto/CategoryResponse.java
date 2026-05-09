package com.spendsmart.category.dto;

import com.spendsmart.category.entity.CategoryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long categoryId;
    private Long userId;
    private String name;
    private CategoryType type;
    private String icon;
    private String colorCode;
    private BigDecimal budgetLimit;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
