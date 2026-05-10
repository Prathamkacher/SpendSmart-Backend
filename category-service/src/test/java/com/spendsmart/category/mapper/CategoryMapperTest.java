package com.spendsmart.category.mapper;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.dto.CategoryResponse;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toEntity() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Groceries");
        request.setType(CategoryType.EXPENSE);
        request.setIcon("cart");
        request.setColorCode("#123456");
        request.setBudgetLimit(new BigDecimal("500.00"));

        Category entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getType(), entity.getType());
        assertEquals(request.getIcon(), entity.getIcon());
        assertEquals(request.getColorCode(), entity.getColorCode());
        assertEquals(request.getBudgetLimit(), entity.getBudgetLimit());
    }

    @Test
    void toEntity_Null() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse() {
        Category entity = new Category();
        entity.setCategoryId(1L);
        entity.setUserId(2L);
        entity.setName("Groceries");
        entity.setType(CategoryType.EXPENSE);
        entity.setIcon("cart");
        entity.setColorCode("#123456");
        entity.setBudgetLimit(new BigDecimal("500.00"));
        entity.setIsDefault(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        CategoryResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(entity.getCategoryId(), response.getCategoryId());
        assertEquals(entity.getUserId(), response.getUserId());
        assertEquals(entity.getName(), response.getName());
        assertEquals(entity.getType(), response.getType());
        assertEquals(entity.getIcon(), response.getIcon());
        assertEquals(entity.getColorCode(), response.getColorCode());
        assertEquals(entity.getBudgetLimit(), response.getBudgetLimit());
        assertEquals(entity.getIsDefault(), response.getIsDefault());
        assertEquals(entity.getCreatedAt(), response.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    void toResponse_Null() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void updateEntityFromRequest() {
        Category entity = new Category();
        entity.setName("Old");

        CategoryRequest request = new CategoryRequest();
        request.setName("New");
        request.setColorCode("#654321");

        mapper.updateEntityFromRequest(request, entity);

        assertEquals("New", entity.getName());
        assertEquals("#654321", entity.getColorCode());
    }

    @Test
    void updateEntityFromRequest_Null() {
        Category entity = new Category();
        entity.setName("Old");
        mapper.updateEntityFromRequest(null, entity);
        assertEquals("Old", entity.getName());
    }
}
