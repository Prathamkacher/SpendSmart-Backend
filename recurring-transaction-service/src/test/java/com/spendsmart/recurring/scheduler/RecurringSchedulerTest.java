package com.spendsmart.recurring.scheduler;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.recurring.exception.GlobalExceptionHandler;
import com.spendsmart.shared.exception.ResourceNotFoundException;
import com.spendsmart.recurring.service.RecurringService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RecurringSchedulerTest {

    @Test
    void processDailyRecurringTransactions_ShouldInvokeServiceAndSwallowFailures() {
        RecurringService recurringService = mock(RecurringService.class);
        RecurringScheduler scheduler = new RecurringScheduler(recurringService);

        scheduler.processDailyRecurringTransactions();
        verify(recurringService).processUpcomingDue();

        doThrow(new IllegalStateException("boom")).when(recurringService).processUpcomingDue();
        scheduler.processDailyRecurringTransactions();

        verify(recurringService, times(2)).processUpcomingDue();
    }

    @Test
    void globalExceptionHandler_ShouldReturnExpectedResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> notFound = handler.handleResourceNotFound(new ResourceNotFoundException("missing"));
        ResponseEntity<ApiResponse<Void>> generic = handler.handleGenericException(new IllegalStateException("boom"));

        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notFound.getBody().getMessage()).isEqualTo("missing");
        assertThat(generic.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(generic.getBody().getMessage()).contains("IllegalStateException").contains("boom");
    }
}
