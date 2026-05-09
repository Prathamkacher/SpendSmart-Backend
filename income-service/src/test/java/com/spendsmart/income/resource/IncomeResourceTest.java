package com.spendsmart.income.resource;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.dto.IncomeResponse;
import com.spendsmart.income.entity.IncomeSource;
import com.spendsmart.income.service.IncomeService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IncomeResource Unit Tests")
class IncomeResourceTest {

    @Mock private IncomeService incomeService;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private IncomeResource incomeResource;

    private IncomeResponse incomeResponse;
    private IncomeRequest incomeRequest;

    @BeforeEach
    void setUp() {
        incomeResponse = new IncomeResponse();
        incomeResponse.setIncomeId(1L);
        incomeResponse.setTitle("Salary");

        incomeRequest = new IncomeRequest();
        incomeRequest.setTitle("Salary");
        incomeRequest.setAmount(new BigDecimal("5000.00"));
        incomeRequest.setSource(IncomeSource.SALARY);
    }

    private void mockUserId() {
        when(httpRequest.getAttribute("userId")).thenReturn(1L);
    }

    @Test
    @DisplayName("addIncome() - should return CREATED")
    void addIncome_ShouldReturnCreated() {
        mockUserId();
        when(incomeService.addIncome(eq(1L), any())).thenReturn(incomeResponse);

        ResponseEntity<ApiResponse<IncomeResponse>> response = incomeResource.addIncome(httpRequest, incomeRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("getIncomeById() - should return OK")
    void getIncomeById_ShouldReturnOk() {
        mockUserId();
        when(incomeService.getIncomeById(1L, 1L)).thenReturn(incomeResponse);

        ResponseEntity<ApiResponse<IncomeResponse>> response = incomeResource.getIncomeById(httpRequest, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getIncomesByUser() - should return OK")
    void getIncomesByUser_ShouldReturnOk() {
        mockUserId();
        when(incomeService.getIncomesByUser(eq(1L), any())).thenReturn(new PageImpl<>(Collections.singletonList(incomeResponse)));

        ResponseEntity<?> response = incomeResource.getIncomesByUser(httpRequest, 0, 10, new String[]{"date", "desc"});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getIncomesBySource() - should return OK")
    void getIncomesBySource_ShouldReturnOk() {
        mockUserId();
        when(incomeService.getIncomesBySource(eq(1L), eq(IncomeSource.SALARY), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(incomeResponse)));

        ResponseEntity<ApiResponse<Page<IncomeResponse>>> response =
                incomeResource.getIncomesBySource(httpRequest, IncomeSource.SALARY, 0, 10, new String[]{"title", "asc"});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(incomeService).getIncomesBySource(eq(1L), eq(IncomeSource.SALARY), argThat(pageable ->
                hasSort(pageable, "title", true)));
    }

    @Test
    @DisplayName("getIncomesByDateRange() - should return OK")
    void getIncomesByDateRange_ShouldReturnOk() {
        mockUserId();
        when(incomeService.getIncomesByDateRange(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(incomeResponse)));

        ResponseEntity<ApiResponse<Page<IncomeResponse>>> response =
                incomeResource.getIncomesByDateRange(
                        httpRequest,
                        java.time.LocalDate.of(2026, 5, 1),
                        java.time.LocalDate.of(2026, 5, 31),
                        0,
                        10,
                        new String[]{"date", "desc"});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getIncomesByMonth() - should return OK")
    void getIncomesByMonth_ShouldReturnOk() {
        mockUserId();
        when(incomeService.getIncomesByMonth(eq(1L), eq(2026), eq(5), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(incomeResponse)));

        ResponseEntity<ApiResponse<Page<IncomeResponse>>> response =
                incomeResource.getIncomesByMonth(httpRequest, 2026, 5, 0, 10, new String[]{"date", "desc"});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateIncome() - should return OK")
    void updateIncome_ShouldReturnOk() {
        mockUserId();
        when(incomeService.updateIncome(eq(1L), eq(1L), any())).thenReturn(incomeResponse);

        ResponseEntity<ApiResponse<IncomeResponse>> response = incomeResource.updateIncome(httpRequest, 1L, incomeRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteIncome() - should return OK")
    void deleteIncome_ShouldReturnOk() {
        mockUserId();

        ResponseEntity<ApiResponse<Void>> response = incomeResource.deleteIncome(httpRequest, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(incomeService).deleteIncome(1L, 1L);
    }

    @Test
    @DisplayName("getTotalIncomeByUser() - should return OK")
    void getTotalIncomeByUser_ShouldReturnOk() {
        mockUserId();
        when(incomeService.getTotalIncomeByUser(1L)).thenReturn(new BigDecimal("10000.00"));

        ResponseEntity<ApiResponse<BigDecimal>> response = incomeResource.getTotalIncomeByUser(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getRecurringIncomes() - should return OK")
    void getRecurringIncomes_ShouldReturnOk() {
        when(incomeService.getRecurringIncomes()).thenReturn(Collections.singletonList(incomeResponse));

        ResponseEntity<?> response = incomeResource.getRecurringIncomes();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getTotalIncomeByMonth() - should return OK")
    void getTotalIncomeByMonth_ShouldReturnOk() {
        mockUserId();
        when(incomeService.getTotalIncomeByMonth(1L, 2026, 5)).thenReturn(new BigDecimal("7500.00"));

        ResponseEntity<ApiResponse<BigDecimal>> response = incomeResource.getTotalIncomeByMonth(httpRequest, 2026, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getAllIncomesForAdmin() - should return OK")
    void getAllIncomesForAdmin_ShouldReturnOk() {
        when(incomeService.getAllIncomes(any())).thenReturn(new PageImpl<>(Collections.singletonList(incomeResponse)));

        ResponseEntity<ApiResponse<Page<IncomeResponse>>> response =
                incomeResource.getAllIncomesForAdmin(0, 50, new String[]{"date", "desc"});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getGlobalStats() - should return OK")
    void getGlobalStats_ShouldReturnOk() {
        when(incomeService.getGlobalTotalIncome()).thenReturn(new BigDecimal("24000.00"));
        when(incomeService.getGlobalIncomeCount()).thenReturn(6L);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = incomeResource.getGlobalStats();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).containsEntry("totalCount", 6L);
    }

    @Test
    @DisplayName("extractUserId() - should throw when null")
    void extractUserId_Null_ShouldThrow() {
        when(httpRequest.getAttribute("userId")).thenReturn(null);
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> incomeResource.addIncome(httpRequest, incomeRequest));
    }

    @Test
    @DisplayName("extractUserId() - should handle Integer")
    void extractUserId_Integer_ShouldConvert() {
        when(httpRequest.getAttribute("userId")).thenReturn(Integer.valueOf(1));
        when(incomeService.getTotalIncomeByUser(1L)).thenReturn(BigDecimal.ZERO);

        ResponseEntity<ApiResponse<BigDecimal>> response = incomeResource.getTotalIncomeByUser(httpRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private boolean hasSort(Pageable pageable, String property, boolean ascending) {
        org.springframework.data.domain.Sort.Order order = pageable.getSort().getOrderFor(property);
        return order != null && order.isAscending() == ascending;
    }
}
