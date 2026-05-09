package com.spendsmart.income.mapper;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.dto.IncomeResponse;
import com.spendsmart.income.entity.Income;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    IncomeResponse toResponse(Income income);

    @Mapping(target = "incomeId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Income toEntity(IncomeRequest request);

    @Mapping(target = "incomeId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Income income, IncomeRequest request);
}
