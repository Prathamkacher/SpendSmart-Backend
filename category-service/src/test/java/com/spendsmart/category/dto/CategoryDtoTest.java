package com.spendsmart.category.dto;

import com.spendsmart.category.entity.CategoryType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDtoTest {

    @Test
    void testCategoryResponseBuilderAndAccessors() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 8, 30);
        CategoryResponse response = buildCategoryResponse(now);

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
    }

    @Test
    void testCategoryResponseEqualityAndAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 8, 30);
        CategoryResponse first = buildCategoryResponse(now);
        CategoryResponse second = new CategoryResponse(
                1L,
                10L,
                "Food",
                CategoryType.EXPENSE,
                "pizza",
                "#FF0000",
                new BigDecimal("500.00"),
                false,
                now,
                now
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(first.toString().contains("Food"));
    }

    @Test
    void testCategoryRequestBuilderAndAccessors() {
        CategoryRequest request = buildCategoryRequest();

        assertEquals("Travel", request.getName());
        assertEquals(CategoryType.EXPENSE, request.getType());
        assertEquals("plane", request.getIcon());
        assertEquals("#0000FF", request.getColorCode());
        assertEquals(new BigDecimal("1000.00"), request.getBudgetLimit());
    }

    @Test
    void testCategoryRequestEqualitySettersAndAllArgsConstructor() {
        CategoryRequest request = buildCategoryRequest();
        CategoryRequest same = new CategoryRequest(
                "Travel",
                CategoryType.EXPENSE,
                "plane",
                "#0000FF",
                new BigDecimal("1000.00")
        );
        CategoryRequest mutable = new CategoryRequest();
        mutable.setName("Travel");
        mutable.setType(CategoryType.EXPENSE);
        mutable.setIcon("plane");
        mutable.setColorCode("#0000FF");
        mutable.setBudgetLimit(new BigDecimal("1000.00"));

        assertEquals(request, same);
        assertEquals(request, mutable);
        assertEquals(request.hashCode(), same.hashCode());
        assertTrue(request.toString().contains("Travel"));
    }

    private static CategoryResponse buildCategoryResponse(LocalDateTime now) {
        return CategoryResponse.builder()
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
    }

    private static CategoryRequest buildCategoryRequest() {
        return CategoryRequest.builder()
                .name("Travel")
                .type(CategoryType.EXPENSE)
                .icon("plane")
                .colorCode("#0000FF")
                .budgetLimit(new BigDecimal("1000.00"))
                .build();
    }
}
