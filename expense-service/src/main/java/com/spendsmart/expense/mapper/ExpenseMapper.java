package com.spendsmart.expense.mapper;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.dto.ExpenseResponse;
import com.spendsmart.expense.entity.Expense;
import org.mapstruct.*;

/**
 * MapStruct mapper for Expense entity <-> DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "expenseId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toEntity(ExpenseRequest request);

    ExpenseResponse toResponse(Expense expense);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "expenseId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ExpenseRequest request, @MappingTarget Expense expense);
}
