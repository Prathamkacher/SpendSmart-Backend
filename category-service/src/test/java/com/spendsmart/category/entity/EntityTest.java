package com.spendsmart.category.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testCategory() {
        Category category = new Category();
        LocalDateTime now = LocalDateTime.now();

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
        assertEquals("Food", category.getName());
        assertEquals(CategoryType.EXPENSE, category.getType());
        assertTrue(category.getIsDefault());
        assertEquals(now, category.getCreatedAt());

        assertNotNull(category.toString());
    }
}
