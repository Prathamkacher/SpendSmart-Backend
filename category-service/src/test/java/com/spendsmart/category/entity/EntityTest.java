package com.spendsmart.category.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testCategorySettersAndGetters() {
        Category category = new Category();
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 9, 45);

        category.setCategoryId(1L);
        category.setUserId(2L);
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setIcon("pizza");
        category.setColorCode("#FF0000");
        category.setBudgetLimit(new BigDecimal("100"));
        category.setIsDefault(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        assertEquals(1L, category.getCategoryId());
        assertEquals(2L, category.getUserId());
        assertEquals("Food", category.getName());
        assertEquals(CategoryType.EXPENSE, category.getType());
        assertEquals("pizza", category.getIcon());
        assertEquals("#FF0000", category.getColorCode());
        assertEquals(new BigDecimal("100"), category.getBudgetLimit());
        assertTrue(category.getIsDefault());
        assertEquals(now, category.getCreatedAt());
        assertEquals(now, category.getUpdatedAt());
        assertTrue(category.toString().contains("Food"));
    }

    @Test
    void testCategoryBuilderDefaultsAndAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 10, 9, 45);
        Category category = Category.builder()
                .categoryId(1L)
                .userId(2L)
                .name("Health")
                .type(CategoryType.EXPENSE)
                .build();
        Category full = new Category(
                5L,
                9L,
                "Salary",
                CategoryType.INCOME,
                "wallet",
                "#123456",
                new BigDecimal("2500.00"),
                true,
                now,
                now
        );

        assertEquals(1L, category.getCategoryId());
        assertEquals("Health", category.getName());
        assertEquals("#6366F1", category.getColorCode());
        assertFalse(category.getIsDefault());

        assertEquals(5L, full.getCategoryId());
        assertEquals(CategoryType.INCOME, full.getType());
        assertEquals("wallet", full.getIcon());
        assertEquals("#123456", full.getColorCode());
        assertTrue(full.getIsDefault());
    }

    @Test
    void testCategoryTypeEnum() {
        assertEquals("EXPENSE", CategoryType.EXPENSE.name());
        assertEquals("INCOME", CategoryType.INCOME.name());
        assertEquals(CategoryType.EXPENSE, CategoryType.valueOf("EXPENSE"));
    }

    @Test
    void testCategoryEqualsHashCode() {
        Category c1 = Category.builder().categoryId(1L).name("A").build();
        Category c2 = Category.builder().categoryId(1L).name("A").build();
        Category c3 = Category.builder().categoryId(2L).name("B").build();

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
