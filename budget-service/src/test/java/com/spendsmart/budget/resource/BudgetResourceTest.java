package com.spendsmart.budget.resource;

import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.BudgetResponse;
import com.spendsmart.budget.service.BudgetService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetResource Unit Tests")
class BudgetResourceTest {

    @Mock private BudgetService budgetService;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private BudgetResource budgetResource;

    private BudgetRequest budgetRequest;
    private BudgetResponse budgetResponse;

    @BeforeEach
    void setUp() {
        budgetRequest = new BudgetRequest();
        budgetRequest.setName("Monthly Food");
        budgetRequest.setLimitAmount(new BigDecimal("1000.00"));

        budgetResponse = new BudgetResponse();
        budgetResponse.setBudgetId(1L);
        budgetResponse.setName("Monthly Food");
    }

    private void mockUserId() {
        when(httpRequest.getAttribute("userId")).thenReturn(1L);
    }

    @Test
    @DisplayName("createBudget() - should return CREATED")
    void createBudget_ShouldReturnCreated() {
        mockUserId();
        when(budgetService.createBudget(eq(1L), any())).thenReturn(budgetResponse);

        ResponseEntity<ApiResponse<BudgetResponse>> response = budgetResource.createBudget(httpRequest, budgetRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("getBudgetById() - should return OK")
    void getBudgetById_ShouldReturnOk() {
        mockUserId();
        when(budgetService.getBudgetById(1L, 1L)).thenReturn(budgetResponse);

        ResponseEntity<ApiResponse<BudgetResponse>> response = budgetResource.getBudgetById(httpRequest, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getActiveBudgets() - should return OK")
    void getActiveBudgets_ShouldReturnOk() {
        mockUserId();
        when(budgetService.getActiveBudgets(1L)).thenReturn(Collections.singletonList(budgetResponse));

        ResponseEntity<ApiResponse<List<BudgetResponse>>> response = budgetResource.getActiveBudgets(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    @DisplayName("deleteBudget() - should return OK")
    void deleteBudget_ShouldReturnOk() {
        mockUserId();

        ResponseEntity<ApiResponse<Void>> response = budgetResource.deleteBudget(httpRequest, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(budgetService).deleteBudget(1L, 1L);
    }

    @Test
    @DisplayName("extractUserId() - should handle Integer")
    void extractUserId_Integer_ShouldConvert() {
        when(httpRequest.getAttribute("userId")).thenReturn(Integer.valueOf(1));
        when(budgetService.getActiveBudgets(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<List<BudgetResponse>>> response = budgetResource.getActiveBudgets(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("extractUserId() - should throw when null")
    void extractUserId_Null_ShouldThrow() {
        when(httpRequest.getAttribute("userId")).thenReturn(null);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> budgetResource.getActiveBudgets(httpRequest));
    }
}
