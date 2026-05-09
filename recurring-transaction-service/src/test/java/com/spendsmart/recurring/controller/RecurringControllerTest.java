package com.spendsmart.recurring.controller;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.recurring.dto.RecurringRequest;
import com.spendsmart.recurring.dto.RecurringResponse;
import com.spendsmart.recurring.entity.Frequency;
import com.spendsmart.recurring.entity.TransactionType;
import com.spendsmart.recurring.service.RecurringService;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurringController Unit Tests")
class RecurringControllerTest {

    @Mock private RecurringService recurringService;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private RecurringController recurringController;

    private RecurringResponse recurringResponse;
    private RecurringRequest recurringRequest;

    @BeforeEach
    void setUp() {
        recurringResponse = RecurringResponse.builder()
                .recurringId(1L)
                .title("Internet")
                .build();

        recurringRequest = new RecurringRequest();
        recurringRequest.setTitle("Internet");
        recurringRequest.setAmount(new BigDecimal("1000"));
        recurringRequest.setType(TransactionType.EXPENSE);
        recurringRequest.setFrequency(Frequency.MONTHLY);
        recurringRequest.setStartDate(LocalDate.now());
    }

    private void mockUserId() {
        when(httpRequest.getHeader("X-User-Id")).thenReturn("1");
    }

    @Test
    @DisplayName("addRecurring() - should return CREATED")
    void addRecurring_ShouldReturnCreated() {
        mockUserId();
        when(recurringService.addRecurring(eq(1L), any())).thenReturn(recurringResponse);

        ResponseEntity<ApiResponse<RecurringResponse>> response = recurringController.addRecurring(recurringRequest, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("getUserRecurring() - should return OK")
    void getUserRecurring_ShouldReturnOk() {
        mockUserId();
        when(recurringService.getByUser(1L)).thenReturn(Collections.singletonList(recurringResponse));

        ResponseEntity<ApiResponse<List<RecurringResponse>>> response = recurringController.getUserRecurring(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    @DisplayName("getRecurringById() - should return OK")
    void getRecurringById_ShouldReturnOk() {
        when(recurringService.getById(1L)).thenReturn(recurringResponse);

        ResponseEntity<ApiResponse<RecurringResponse>> response = recurringController.getRecurringById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getActiveRecurring() - should return OK")
    void getActiveRecurring_ShouldReturnOk() {
        mockUserId();
        when(recurringService.getActiveRecurring(1L)).thenReturn(Collections.singletonList(recurringResponse));

        ResponseEntity<ApiResponse<List<RecurringResponse>>> response = recurringController.getActiveRecurring(httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateRecurring() - should return OK")
    void updateRecurring_ShouldReturnOk() {
        mockUserId();
        when(recurringService.updateRecurring(eq(1L), eq(1L), any())).thenReturn(recurringResponse);

        ResponseEntity<ApiResponse<RecurringResponse>> response = recurringController.updateRecurring(1L, recurringRequest, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deactivateRecurring() - should return OK")
    void deactivateRecurring_ShouldReturnOk() {
        mockUserId();
        when(recurringService.deactivateRecurring(1L, 1L)).thenReturn(recurringResponse);

        ResponseEntity<ApiResponse<RecurringResponse>> response = recurringController.deactivateRecurring(1L, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteRecurring() - should return OK")
    void deleteRecurring_ShouldReturnOk() {
        mockUserId();

        ResponseEntity<ApiResponse<Void>> response = recurringController.deleteRecurring(1L, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(recurringService).deleteRecurring(1L, 1L);
    }

    @Test
    @DisplayName("triggerScheduler() - should return OK")
    void triggerScheduler_ShouldReturnOk() {
        ResponseEntity<ApiResponse<Void>> response = recurringController.triggerScheduler();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(recurringService).processUpcomingDue();
    }

    @Test
    @DisplayName("getUserIdFromHeader() - should throw when missing")
    void getUserIdFromHeader_Missing_ShouldThrow() {
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> recurringController.getUserRecurring(httpRequest));
    }
}
