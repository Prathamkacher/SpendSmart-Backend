package com.spendsmart.shared.exception;

import com.spendsmart.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseGlobalExceptionHandlerTest {

    private final TestExceptionHandler handler = new TestExceptionHandler();

    @Test
    void handleValidationErrorsShouldReturnBadRequestWithFieldMessages() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Email is required"));
        bindingResult.addError(new FieldError("request", "password", "Password is too short"));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handleValidationErrors(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getData())
                .containsEntry("email", "Email is required")
                .containsEntry("password", "Password is too short");
    }

    @Test
    void handleResourceNotFoundShouldReturnNotFound() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleResourceNotFound(new ResourceNotFoundException("Budget not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Budget not found");
    }

    @Test
    void handleUnauthorizedAccessShouldReturnForbidden() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleUnauthorizedAccess(new UnauthorizedAccessException("Forbidden action"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Forbidden action");
    }

    @Test
    void handleIllegalArgumentShouldReturnBadRequest() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Invalid amount"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid amount");
    }

    @Test
    void handleGenericExceptionShouldIncludeExceptionTypeAndMessage() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleGenericException(new IllegalStateException("Unexpected state"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .contains("IllegalStateException")
                .contains("Unexpected state");
    }

    @Test
    void exceptionClassesShouldExposeTheirMessages() {
        assertThat(new ResourceNotFoundException("missing").getMessage()).isEqualTo("missing");
        assertThat(new UnauthorizedAccessException("denied").getMessage()).isEqualTo("denied");
    }

    private static class TestExceptionHandler extends BaseGlobalExceptionHandler {
    }
}
