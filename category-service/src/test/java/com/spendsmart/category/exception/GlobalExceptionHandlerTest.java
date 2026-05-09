package com.spendsmart.category.exception;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("handleCategoryNotFound - should return 404")
    void handleCategoryNotFound() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleCategoryNotFound(new CategoryNotFoundException(999L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    @DisplayName("handleUnauthorizedAccess - should return 403")
    void handleUnauthorizedAccess() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorizedAccess(new UnauthorizedAccessException("Forbidden"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("handleDuplicateCategory - should return 409")
    void handleDuplicateCategory() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateCategory(new DuplicateCategoryException("Duplicate"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("handleIllegalArgument - should return 400")
    void handleIllegalArgument() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(new IllegalArgumentException("Bad"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("handleGenericException - should return 500")
    void handleGenericException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(new RuntimeException("Oops"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
