package com.spendsmart.expense.resource;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.dto.ExpenseResponse;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import com.spendsmart.expense.service.ExpenseService;
import com.spendsmart.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseResource Unit Tests")
class ExpenseResourceTest {

    @Mock private ExpenseService expenseService;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private ExpenseResource expenseResource;

    private ExpenseResponse expenseResponse;

    @BeforeEach
    void setUp() {
        expenseResponse = new ExpenseResponse();
        expenseResponse.setExpenseId(1L);
        expenseResponse.setTitle("Lunch");
    }

    private void mockUserId() {
        when(httpRequest.getAttribute("userId")).thenReturn(1L);
    }

    @Test
    @DisplayName("addExpense() - should return CREATED")
    void addExpense_ShouldReturnCreated() {
        mockUserId();
        when(expenseService.addExpense(eq(1L), any())).thenReturn(expenseResponse);

        ExpenseRequest req = new ExpenseRequest();
        ResponseEntity<ApiResponse<ExpenseResponse>> response = expenseResource.addExpense(httpRequest, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("getExpenses() - should return OK")
    void getExpenses_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getExpensesByUser(eq(1L), any())).thenReturn(new PageImpl<>(Collections.singletonList(expenseResponse)));

        ResponseEntity<ApiResponse<Page<ExpenseResponse>>> response =
                expenseResource.getExpenses(httpRequest, 0, 20, "date", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getExpenseById() - should return OK")
    void getExpenseById_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getExpenseById(1L, 1L)).thenReturn(expenseResponse);

        ResponseEntity<ApiResponse<ExpenseResponse>> response = expenseResource.getExpenseById(httpRequest, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getExpensesByUser() - should return OK when user accesses own data")
    void getExpensesByUser_ShouldReturnOkWhenAuthorized() {
        mockUserId();
        when(expenseService.getExpensesByUser(eq(1L), any())).thenReturn(new PageImpl<>(Collections.singletonList(expenseResponse)));

        ResponseEntity<ApiResponse<Page<ExpenseResponse>>> response =
                expenseResource.getExpensesByUser(httpRequest, 1L, 0, 10, "title", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(expenseService).getExpensesByUser(eq(1L), argThat(pageable ->
                hasSort(pageable, "title", true)));
    }

    @Test
    @DisplayName("getExpensesByUser() - should reject mismatched user access")
    void getExpensesByUser_ShouldThrowWhenUnauthorized() {
        mockUserId();

        assertThatThrownBy(() -> expenseResource.getExpensesByUser(httpRequest, 2L, 0, 10, "date", "desc"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("getExpensesByCategory() - should return OK")
    void getExpensesByCategory_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getExpensesByCategory(eq(1L), eq(7L), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(expenseResponse)));

        ResponseEntity<ApiResponse<Page<ExpenseResponse>>> response =
                expenseResource.getExpensesByCategory(httpRequest, 7L, 0, 20, "amount", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getExpensesByDateRange() - should return OK")
    void getExpensesByDateRange_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getExpensesByDateRange(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(expenseResponse)));

        ResponseEntity<ApiResponse<Page<ExpenseResponse>>> response =
                expenseResource.getExpensesByDateRange(
                        httpRequest,
                        java.time.LocalDate.of(2026, 5, 1),
                        java.time.LocalDate.of(2026, 5, 31),
                        0,
                        20,
                        "date",
                        "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getExpensesByMonth() - should return OK")
    void getExpensesByMonth_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getExpensesByMonth(eq(1L), eq(2026), eq(5), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(expenseResponse)));

        ResponseEntity<ApiResponse<Page<ExpenseResponse>>> response =
                expenseResource.getExpensesByMonth(httpRequest, 2026, 5, 0, 20, "date", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getExpensesByType() - should return OK")
    void getExpensesByType_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getExpensesByType(eq(1L), eq(ExpenseType.EXPENSE), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(expenseResponse)));

        ResponseEntity<ApiResponse<Page<ExpenseResponse>>> response =
                expenseResource.getExpensesByType(httpRequest, ExpenseType.EXPENSE, 0, 20, "date", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("searchExpenses() - should return OK")
    void searchExpenses_ShouldReturnOk() {
        mockUserId();
        when(expenseService.searchExpenses(eq(1L), eq("lunch"), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(expenseResponse)));

        ResponseEntity<ApiResponse<Page<ExpenseResponse>>> response =
                expenseResource.searchExpenses(httpRequest, "lunch", 0, 20, "date", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateExpense() - should return OK")
    void updateExpense_ShouldReturnOk() {
        mockUserId();
        when(expenseService.updateExpense(eq(1L), eq(1L), any())).thenReturn(expenseResponse);

        ExpenseRequest req = new ExpenseRequest();
        ResponseEntity<ApiResponse<ExpenseResponse>> response = expenseResource.updateExpense(httpRequest, 1L, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteExpense() - should return OK")
    void deleteExpense_ShouldReturnOk() {
        mockUserId();

        ResponseEntity<ApiResponse<Void>> response = expenseResource.deleteExpense(httpRequest, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(expenseService).deleteExpense(1L, 1L);
    }

    @Test
    @DisplayName("getTotalByUser() - should return OK")
    void getTotalByUser_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getTotalByUser(1L)).thenReturn(new BigDecimal("5000"));

        ResponseEntity<ApiResponse<BigDecimal>> response = expenseResource.getTotalByUser(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getTotalByCategory() - should return OK")
    void getTotalByCategory_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getTotalByCategory(1L, 9L)).thenReturn(new BigDecimal("2500"));

        ResponseEntity<ApiResponse<BigDecimal>> response = expenseResource.getTotalByCategory(httpRequest, 9L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getTotalByMonth() - should return OK")
    void getTotalByMonth_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getTotalByMonth(1L, 2026, 5)).thenReturn(new BigDecimal("3200"));

        ResponseEntity<ApiResponse<BigDecimal>> response = expenseResource.getTotalByMonth(httpRequest, 2026, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getCategoryBreakdown() - should return OK")
    void getCategoryBreakdown_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getCategoryBreakdown(1L, 2026, 5)).thenReturn(Map.of("Food", new BigDecimal("1200")));

        ResponseEntity<ApiResponse<Map<String, BigDecimal>>> response =
                expenseResource.getCategoryBreakdown(httpRequest, 2026, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).containsKey("Food");
    }

    @Test
    @DisplayName("getDailyTrend() - should return OK")
    void getDailyTrend_ShouldReturnOk() {
        mockUserId();
        when(expenseService.getDailyTrend(1L, 2026, 5)).thenReturn(Map.of("2026-05-01", new BigDecimal("300")));

        ResponseEntity<ApiResponse<Map<String, BigDecimal>>> response =
                expenseResource.getDailyTrend(httpRequest, 2026, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).containsKey("2026-05-01");
    }

    @Test
    @DisplayName("getAllExpensesForAdmin() - should return OK")
    void getAllExpensesForAdmin_ShouldReturnOk() {
        when(expenseService.getAllExpenses(any())).thenReturn(new PageImpl<>(Collections.singletonList(expenseResponse)));

        ResponseEntity<ApiResponse<Page<ExpenseResponse>>> response =
                expenseResource.getAllExpensesForAdmin(0, 50, "date", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getGlobalStats() - should return OK")
    void getGlobalStats_ShouldReturnOk() {
        when(expenseService.getGlobalTotalExpenses()).thenReturn(new BigDecimal("15000"));
        when(expenseService.getGlobalExpenseCount()).thenReturn(12L);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = expenseResource.getGlobalStats();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).containsEntry("totalCount", 12L);
    }

    @Test
    @DisplayName("extractUserId() - should handle Integer")
    void extractUserId_Integer_ShouldConvert() {
        when(httpRequest.getAttribute("userId")).thenReturn(Integer.valueOf(1));
        when(expenseService.getTotalByUser(1L)).thenReturn(BigDecimal.ZERO);

        ResponseEntity<ApiResponse<BigDecimal>> response = expenseResource.getTotalByUser(httpRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("extractUserId() - should throw when null")
    void extractUserId_Null_ShouldThrow() {
        when(httpRequest.getAttribute("userId")).thenReturn(null);
        org.junit.jupiter.api.Assertions.assertThrows(UnauthorizedAccessException.class,
                () -> expenseResource.deleteExpense(httpRequest, 1L));
    }

    private boolean hasSort(Pageable pageable, String property, boolean ascending) {
        Sort.Order order = pageable.getSort().getOrderFor(property);
        return order != null && order.isAscending() == ascending;
    }
}
