package com.spendsmart.category.service;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.dto.CategoryResponse;
import com.spendsmart.category.entity.CategoryType;

import java.math.BigDecimal;
import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(Long userId, CategoryRequest request);

    List<CategoryResponse> getByUser(Long userId);

    CategoryResponse getCategoryById(Long userId, Long categoryId);

    List<CategoryResponse> getByUserAndType(Long userId, CategoryType type);

    CategoryResponse updateCategory(Long userId, Long categoryId, CategoryRequest request);

    void deleteCategory(Long userId, Long categoryId);

    List<CategoryResponse> getDefaultCategories();

    void initDefaultCategories(Long userId);

    CategoryResponse setCategoryBudget(Long userId, Long categoryId, BigDecimal budgetLimit);

    long getCategoryCount(Long userId);

    java.util.Map<Long, String> getCategoryNames(Long userId);
}
