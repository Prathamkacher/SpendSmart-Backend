package com.spendsmart.category.resource;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.dto.CategoryResponse;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import com.spendsmart.category.service.CategoryService;
import com.spendsmart.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryResource Unit Tests")
class CategoryResourceTest {

    @Mock private CategoryService categoryService;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private CategoryResource categoryResource;

    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Food");
        categoryRequest.setType(CategoryType.EXPENSE);

        categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryId(1L);
        categoryResponse.setName("Food");
    }

    private void mockUserId() {
        when(httpRequest.getAttribute("userId")).thenReturn(userId);
    }

    @Test
    @DisplayName("createCategory() - should return CREATED")
    void createCategory_ShouldReturnCreated() {
        mockUserId();
        when(categoryService.createCategory(eq(userId), any())).thenReturn(categoryResponse);

        ResponseEntity<ApiResponse<CategoryResponse>> response = categoryResource.createCategory(httpRequest, categoryRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("getUserCategories() - should return OK")
    void getUserCategories_ShouldReturnOk() {
        mockUserId();
        when(categoryService.getByUser(userId)).thenReturn(Collections.singletonList(categoryResponse));

        ResponseEntity<ApiResponse<List<CategoryResponse>>> response = categoryResource.getUserCategories(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    @DisplayName("getCategoryById() - should return OK")
    void getCategoryById_ShouldReturnOk() {
        mockUserId();
        when(categoryService.getCategoryById(userId, 1L)).thenReturn(categoryResponse);

        ResponseEntity<ApiResponse<CategoryResponse>> response = categoryResource.getCategoryById(httpRequest, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getCategoriesByType() - should return OK")
    void getCategoriesByType_ShouldReturnOk() {
        mockUserId();
        when(categoryService.getByUserAndType(userId, CategoryType.EXPENSE)).thenReturn(Collections.singletonList(categoryResponse));

        ResponseEntity<ApiResponse<List<CategoryResponse>>> response = categoryResource.getCategoriesByType(httpRequest, CategoryType.EXPENSE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getDefaultCategories() - should return OK")
    void getDefaultCategories_ShouldReturnOk() {
        when(categoryService.getDefaultCategories()).thenReturn(Collections.singletonList(categoryResponse));

        ResponseEntity<ApiResponse<List<CategoryResponse>>> response = categoryResource.getDefaultCategories();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateCategory() - should return OK")
    void updateCategory_ShouldReturnOk() {
        mockUserId();
        when(categoryService.updateCategory(eq(userId), eq(1L), any())).thenReturn(categoryResponse);

        ResponseEntity<ApiResponse<CategoryResponse>> response = categoryResource.updateCategory(httpRequest, 1L, categoryRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("setCategoryBudget() - should return OK")
    void setCategoryBudget_ShouldReturnOk() {
        mockUserId();
        when(categoryService.setCategoryBudget(eq(userId), eq(1L), any())).thenReturn(categoryResponse);

        ResponseEntity<ApiResponse<CategoryResponse>> response = categoryResource.setCategoryBudget(httpRequest, 1L, Map.of("budgetLimit", new BigDecimal("500")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteCategory() - should return OK")
    void deleteCategory_ShouldReturnOk() {
        mockUserId();

        ResponseEntity<ApiResponse<Void>> response = categoryResource.deleteCategory(httpRequest, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(categoryService).deleteCategory(userId, 1L);
    }

    @Test
    @DisplayName("getCategoryCount() - should return OK")
    void getCategoryCount_ShouldReturnOk() {
        mockUserId();
        when(categoryService.getCategoryCount(userId)).thenReturn(5L);

        ResponseEntity<ApiResponse<Long>> response = categoryResource.getCategoryCount(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo(5L);
    }

    @Test
    @DisplayName("initDefaults() - should return CREATED")
    void initDefaults_ShouldReturnCreated() {
        mockUserId();

        ResponseEntity<ApiResponse<Void>> response = categoryResource.initDefaults(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(categoryService).initDefaultCategories(userId);
    }

    @Test
    @DisplayName("getCategoryNames() - should return OK")
    void getCategoryNames_ShouldReturnOk() {
        mockUserId();
        when(categoryService.getCategoryNames(userId)).thenReturn(Map.of(1L, "Food"));

        ResponseEntity<ApiResponse<Map<Long, String>>> response = categoryResource.getCategoryNames(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("extractUserId() - should throw when userId is null")
    void extractUserId_NullUserId_ShouldThrow() {
        when(httpRequest.getAttribute("userId")).thenReturn(null);

        assertThatThrownBy(() -> categoryResource.createCategory(httpRequest, categoryRequest))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("extractUserId() - should handle Integer userId")
    void extractUserId_IntegerUserId_ShouldConvert() {
        when(httpRequest.getAttribute("userId")).thenReturn(Integer.valueOf(1));
        when(categoryService.getByUser(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<List<CategoryResponse>>> response = categoryResource.getUserCategories(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
