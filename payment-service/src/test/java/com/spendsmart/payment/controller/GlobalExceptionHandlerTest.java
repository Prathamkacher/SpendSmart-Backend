package com.spendsmart.payment.controller;

import com.spendsmart.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleException_ShouldReturn500ErrorResponse() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("RuntimeException").contains("boom");
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn400ErrorResponse() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(new IllegalArgumentException("bad request"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("bad request");
    }
}
