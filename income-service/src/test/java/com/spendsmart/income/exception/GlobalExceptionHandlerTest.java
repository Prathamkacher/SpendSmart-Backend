package com.spendsmart.income.exception;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
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
    void handleValidationErrors_ShouldReturnFieldMap() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "income");
        bindingResult.addError(new FieldError("income", "title", "Title is required"));
        Method method = SampleController.class.getDeclaredMethod("sample", String.class);
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(new org.springframework.core.MethodParameter(method, 0), bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handleValidationErrors(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getData()).containsEntry("title", "Title is required");
    }

    @Test
    void specificHandlers_ShouldReturnExpectedStatusCodes() {
        assertThat(handler.handleIncomeNotFound(new IncomeNotFoundException("missing")).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleUnauthorizedAccess(new UnauthorizedAccessException("denied")).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(handler.handleIllegalArgument(new IllegalArgumentException("bad")).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void genericHandler_ShouldReturn500WithFormattedMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(new IllegalStateException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("IllegalStateException").contains("boom");
    }

    static class SampleController {
        @SuppressWarnings("unused")
        public void sample(String input) {
        }
    }
}
