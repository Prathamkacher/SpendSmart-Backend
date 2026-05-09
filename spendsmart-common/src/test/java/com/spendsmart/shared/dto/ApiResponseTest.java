package com.spendsmart.shared.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessWithMessageAndData() {
        String message = "Success";
        String data = "Test Data";
        ApiResponse<String> response = ApiResponse.success(message, data);

        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccessWithMessageOnly() {
        String message = "Success Only";
        ApiResponse<Void> response = ApiResponse.success(message);

        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorWithMessage() {
        String message = "Error occurred";
        ApiResponse<Void> response = ApiResponse.error(message);

        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ApiResponse<Integer> response = new ApiResponse<>();
        LocalDateTime now = LocalDateTime.now();
        
        response.setSuccess(true);
        response.setMessage("Manual");
        response.setData(123);
        response.setTimestamp(now);

        assertTrue(response.isSuccess());
        assertEquals("Manual", response.getMessage());
        assertEquals(123, response.getData());
        assertEquals(now, response.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ApiResponse<String> response = new ApiResponse<>(true, "All", "Data", now);

        assertTrue(response.isSuccess());
        assertEquals("All", response.getMessage());
        assertEquals("Data", response.getData());
        assertEquals(now, response.getTimestamp());
    }

    @Test
    void testBuilder() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Builder")
                .data("Value")
                .build();

        assertTrue(response.isSuccess());
        assertEquals("Builder", response.getMessage());
        assertEquals("Value", response.getData());
    }

    @Test
    void testToStringAndEquals() {
        ApiResponse<String> response1 = ApiResponse.success("Msg", "Data");
        ApiResponse<String> response2 = ApiResponse.success("Msg", "Data");
        
        // Note: timestamp might differ by a tiny bit if created separately, 
        // but here they are likely identical if run quickly enough or we can set them.
        LocalDateTime now = LocalDateTime.now();
        response1.setTimestamp(now);
        response2.setTimestamp(now);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotNull(response1.toString());
    }
}
