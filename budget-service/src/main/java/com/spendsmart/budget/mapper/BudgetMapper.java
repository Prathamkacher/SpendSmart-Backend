package com.spendsmart.budget.mapper;

import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.BudgetResponse;
import com.spendsmart.budget.entity.Budget;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * MapStruct mapper for converting between Budget entities and DTOs.
 * Includes custom logic for calculating budget progress and status.
 */
@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(target = "budgetId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "spentAmount", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Budget toEntity(BudgetRequest request);

    @Mapping(target = "progressPercentage", ignore = true)
    @Mapping(target = "remainingAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    BudgetResponse toResponse(Budget budget);

    @AfterMapping
    default void calculateFields(Budget budget, @MappingTarget BudgetResponse response) {
        BigDecimal limit = budget.getLimitAmount();
        BigDecimal spent = budget.getSpentAmount();
        
        if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
            double percentage = spent.multiply(new BigDecimal("100"))
                    .divide(limit, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            response.setProgressPercentage(percentage);
            response.setRemainingAmount(limit.subtract(spent));
            
            if (percentage >= 100) {
                response.setStatus("EXCEEDED");
            } else if (percentage >= (budget.getAlertThreshold() != null ? budget.getAlertThreshold() : 80)) {
                response.setStatus("WARNING");
            } else {
                response.setStatus("STABLE");
            }
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "budgetId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "spentAmount", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(BudgetRequest request, @MappingTarget Budget budget);
}
