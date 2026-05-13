package com.spendsmart.category.mapper;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.dto.CategoryResponse;
import com.spendsmart.category.entity.Category;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between {@link Category} entity and its DTOs.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Converts a {@link CategoryRequest} to a {@link Category} entity.
     * Ignores system-managed fields like categoryId, userId, and timestamps.
     *
     * @param request The category request DTO.
     * @return The category entity.
     */
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequest request);

    /**
     * Converts a {@link Category} entity to a {@link CategoryResponse} DTO.
     *
     * @param category The category entity.
     * @return The category response DTO.
     */
    CategoryResponse toResponse(Category category);

    /**
     * Updates an existing {@link Category} entity from a {@link CategoryRequest}.
     * Only non-null values in the request are mapped.
     *
     * @param request The category request DTO.
     * @param category The target category entity to update.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
