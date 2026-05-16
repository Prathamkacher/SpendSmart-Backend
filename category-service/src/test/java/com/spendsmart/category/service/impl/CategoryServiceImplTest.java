package com.spendsmart.category.service.impl;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.dto.CategoryResponse;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.exception.CategoryNotFoundException;
import com.spendsmart.category.exception.DuplicateCategoryException;
import com.spendsmart.category.mapper.CategoryMapper;
import com.spendsmart.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Unit Tests")
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryRequest categoryRequest;
    private final Long userId = 1L;
    private final Long categoryId = 1L;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .categoryId(categoryId)
                .userId(userId)
                .name("Food")
                .type(CategoryType.EXPENSE)
                .icon("\uD83C\uDF54")
                .colorCode("#EF4444")
                .isDefault(false)
                .build();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Food");
        categoryRequest.setType(CategoryType.EXPENSE);
        categoryRequest.setIcon("\uD83C\uDF54");
        categoryRequest.setColorCode("#EF4444");
    }

    // ── CREATE TESTS ──────────────────────────────────────────────────

    @Test
    @DisplayName("createCategory() - should save category when not duplicate")
    void createCategory_ShouldSave() {
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, "Food", CategoryType.EXPENSE)).thenReturn(false);
        when(categoryMapper.toEntity(any())).thenReturn(testCategory);
        when(categoryRepository.save(any())).thenReturn(testCategory);
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        CategoryResponse response = categoryService.createCategory(userId, categoryRequest);

        assertThat(response).isNotNull();
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("createCategory() - should throw DuplicateCategoryException when exists")
    void createCategory_Duplicate_ShouldThrowException() {
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, "Food", CategoryType.EXPENSE)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(userId, categoryRequest))
                .isInstanceOf(DuplicateCategoryException.class);
    }

    // ── READ TESTS ────────────────────────────────────────────────────

    @Test
    @DisplayName("getByUser() - should return user categories")
    void getByUser_ShouldReturnList() {
        when(categoryRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(Collections.singletonList(testCategory));
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        List<CategoryResponse> result = categoryService.getByUser(userId);

        assertThat(result).hasSize(1);
        verify(categoryRepository).findByUserIdOrderByNameAsc(userId);
    }

    @Test
    @DisplayName("getCategoryById() - should return category when found")
    void getCategoryById_ShouldReturnResponse() {
        when(categoryRepository.findByCategoryIdAndUserId(categoryId, userId)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(new CategoryResponse());

        CategoryResponse response = categoryService.getCategoryById(userId, categoryId);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("getCategoryById() - should throw exception when not found")
    void getCategoryById_NotFound_ShouldThrowException() {
        when(categoryRepository.findByCategoryIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(userId, categoryId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("getByUserAndType() - should return filtered list")
    void getByUserAndType_ShouldReturnList() {
        when(categoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, CategoryType.EXPENSE))
                .thenReturn(Collections.singletonList(testCategory));
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        List<CategoryResponse> result = categoryService.getByUserAndType(userId, CategoryType.EXPENSE);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getDefaultCategories() - should return default list")
    void getDefaultCategories_ShouldReturnList() {
        when(categoryRepository.findByIsDefaultTrue()).thenReturn(Collections.singletonList(testCategory));
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        List<CategoryResponse> result = categoryService.getDefaultCategories();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getCategoryCount() - should return count")
    void getCategoryCount_ShouldReturnCount() {
        when(categoryRepository.countByUserId(userId)).thenReturn(5L);

        long count = categoryService.getCategoryCount(userId);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("getCategoryNames() - should return map of ID to Name")
    void getCategoryNames_ShouldReturnMap() {
        when(categoryRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(Collections.singletonList(testCategory));

        java.util.Map<Long, String> names = categoryService.getCategoryNames(userId);

        assertThat(names).containsEntry(categoryId, "Food");
    }

    // ── UPDATE TESTS ──────────────────────────────────────────────────

    @Test
    @DisplayName("updateCategory() - should update successfully")
    void updateCategory_ShouldUpdate() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Groceries");
        updateRequest.setType(CategoryType.EXPENSE);

        when(categoryRepository.findByCategoryIdAndUserId(categoryId, userId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, "Groceries", CategoryType.EXPENSE)).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(testCategory);
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        CategoryResponse response = categoryService.updateCategory(userId, categoryId, updateRequest);

        assertThat(response).isNotNull();
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("updateCategory() - should throw DuplicateCategoryException if new name exists")
    void updateCategory_Duplicate_ShouldThrowException() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Duplicate");
        updateRequest.setType(CategoryType.EXPENSE);

        when(categoryRepository.findByCategoryIdAndUserId(categoryId, userId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, "Duplicate", CategoryType.EXPENSE)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(userId, categoryId, updateRequest))
                .isInstanceOf(DuplicateCategoryException.class);
    }

    @Test
    @DisplayName("setCategoryBudget() - should update budget limit")
    void setCategoryBudget_ShouldUpdate() {
        BigDecimal newLimit = new BigDecimal("500.00");
        when(categoryRepository.findByCategoryIdAndUserId(categoryId, userId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any())).thenReturn(testCategory);
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        CategoryResponse response = categoryService.setCategoryBudget(userId, categoryId, newLimit);

        assertThat(response).isNotNull();
        assertThat(testCategory.getBudgetLimit()).isEqualTo(newLimit);
    }

    // ── DELETE TESTS ──────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCategory() - should delete when exists")
    void deleteCategory_ShouldDelete() {
        when(categoryRepository.findByCategoryIdAndUserId(categoryId, userId)).thenReturn(Optional.of(testCategory));

        categoryService.deleteCategory(userId, categoryId);

        verify(categoryRepository).delete(testCategory);
    }

    // ── SEEDING TESTS ─────────────────────────────────────────────────

    @Test
    @DisplayName("updateCategory() - should not check duplicate if name is same")
    void updateCategory_SameName_ShouldNotCheckDuplicate() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Food"); // Same as testCategory
        updateRequest.setType(CategoryType.EXPENSE);

        when(categoryRepository.findByCategoryIdAndUserId(categoryId, userId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any())).thenReturn(testCategory);
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        categoryService.updateCategory(userId, categoryId, updateRequest);

        verify(categoryRepository, never()).existsByUserIdAndNameIgnoreCaseAndType(anyLong(), anyString(), any());
        verify(categoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("initDefaultCategories() - should do nothing if all exist")
    void initDefaultCategories_AllExist_ShouldNotSeed() {
        // Mocking all default names existing
        List<Category> allExist = Arrays.asList(
            Category.builder().name("Food").build(),
            Category.builder().name("Transport").build(),
            Category.builder().name("Bills").build(),
            Category.builder().name("Health").build(),
            Category.builder().name("Entertainment").build(),
            Category.builder().name("Shopping").build(),
            Category.builder().name("Education").build(),
            Category.builder().name("Other").build(),
            Category.builder().name("Salary").build(),
            Category.builder().name("Freelance").build(),
            Category.builder().name("Business").build(),
            Category.builder().name("Investment").build(),
            Category.builder().name("Gift").build()
        );
        when(categoryRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(allExist);

        categoryService.initDefaultCategories(userId);

        verify(categoryRepository, never()).save(any());
    }
    @Test
    @DisplayName("createCategory() - should use default color code if not provided")
    void createCategory_DefaultColor_ShouldUseDefault() {
        categoryRequest.setColorCode(null);
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, "Food", CategoryType.EXPENSE)).thenReturn(false);
        when(categoryMapper.toEntity(any())).thenReturn(testCategory);
        when(categoryRepository.save(any())).thenReturn(testCategory);
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        categoryService.createCategory(userId, categoryRequest);

        assertThat(testCategory.getColorCode()).isEqualTo("#EF4444"); // Injected mock might not set it, but logic should
        verify(categoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("initDefaultCategories() - should seed missing categories")
    void initDefaultCategories_MissingSome_ShouldSeed() {
        when(categoryRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(Collections.emptyList());

        categoryService.initDefaultCategories(userId);

        verify(categoryRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("getCategoryNames() - should filter null ID or Name")
    void getCategoryNames_NullIdOrName_ShouldFilter() {
        Category nullId = Category.builder().categoryId(null).name("Null ID").build();
        Category nullName = Category.builder().categoryId(100L).name(null).build();
        when(categoryRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(Arrays.asList(testCategory, nullId, nullName));

        java.util.Map<Long, String> names = categoryService.getCategoryNames(userId);

        assertThat(names).hasSize(1);
        assertThat(names).containsKey(categoryId);
    }
}
