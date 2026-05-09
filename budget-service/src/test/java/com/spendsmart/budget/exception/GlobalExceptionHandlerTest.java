package com.spendsmart.budget.exception;

import com.spendsmart.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBudgetNotFound_ShouldReturn404() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBudgetNotFound(new BudgetNotFoundException(7L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("7");
    }

    @Test
    void handleIllegalArgument_ShouldReturn400() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("bad input");
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(new IllegalStateException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("IllegalStateException");
    }

    @Test
    void handleValidationErrors_ShouldReturnFieldMap() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "budget");
        bindingResult.addError(new FieldError("budget", "name", "Name is required"));
        Method method = SampleController.class.getDeclaredMethod("sample", String.class);
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(new org.springframework.core.MethodParameter(method, 0), bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handleValidationErrors(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getData()).containsEntry("name", "Name is required");
    }

    static class SampleController {
        @SuppressWarnings("unused")
        public void sample(String input) {
            // Stub method used only to build a MethodParameter for validation tests.
        }
    }
}
