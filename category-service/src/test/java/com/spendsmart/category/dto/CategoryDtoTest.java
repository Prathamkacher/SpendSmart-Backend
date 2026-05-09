package com.spendsmart.category.dto;

import com.spendsmart.category.entity.CategoryType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CategoryDtoTest {

    @Test
    void testCategoryResponse() {
        LocalDateTime now = LocalDateTime.now();
        CategoryResponse response = CategoryResponse.builder()
                .categoryId(1L)
                .userId(10L)
                .name("Food")
                .type(CategoryType.EXPENSE)
                .icon("pizza")
                .colorCode("#FF0000")
                .budgetLimit(new BigDecimal("500.00"))
                .isDefault(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(1L, response.getCategoryId());
        assertEquals(10L, response.getUserId());
        assertEquals("Food", response.getName());
        assertEquals(CategoryType.EXPENSE, response.getType());
        assertEquals("pizza", response.getIcon());
        assertEquals("#FF0000", response.getColorCode());
        assertEquals(new BigDecimal("500.00"), response.getBudgetLimit());
        assertFalse(response.getIsDefault());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());

        CategoryResponse empty = new CategoryResponse();
        assertNotNull(empty.toString());
        assertNotEquals(response, empty);
    }

    @Test
    void testCategoryRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Travel");
        request.setType(CategoryType.EXPENSE);
        request.setIcon("plane");
        request.setColorCode("#0000FF");
        request.setBudgetLimit(new BigDecimal("1000.00"));

        assertEquals("Travel", request.getName());
        assertEquals(CategoryType.EXPENSE, request.getType());
        assertEquals("plane", request.getIcon());
        assertEquals("#0000FF", request.getColorCode());
        assertEquals(new BigDecimal("1000.00"), request.getBudgetLimit());
        
        assertNotNull(request.toString());
    }
}
