package com.spendsmart.recurring.controller;

import com.spendsmart.recurring.dto.RecurringRequest;
import com.spendsmart.recurring.dto.RecurringResponse;
import com.spendsmart.recurring.service.RecurringService;
import com.spendsmart.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringController {

    private final RecurringService recurringService;

    private Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            return Long.parseLong(userIdStr);
        }
        throw new RuntimeException("User ID not found in request headers");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringResponse>> addRecurring(
            @Valid @RequestBody RecurringRequest request,
            HttpServletRequest servletRequest) {
        Long userId = getUserIdFromHeader(servletRequest);
        RecurringResponse response = recurringService.addRecurring(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring transaction created successfully", response));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<RecurringResponse>>> getUserRecurring(
            HttpServletRequest servletRequest) {
        Long userId = getUserIdFromHeader(servletRequest);
        List<RecurringResponse> responses = recurringService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User recurring transactions fetched", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringResponse>> getRecurringById(@PathVariable Long id) {
        RecurringResponse response = recurringService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction fetched", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RecurringResponse>>> getActiveRecurring(
            HttpServletRequest servletRequest) {
        Long userId = getUserIdFromHeader(servletRequest);
        List<RecurringResponse> responses = recurringService.getActiveRecurring(userId);
        return ResponseEntity.ok(ApiResponse.success("Active recurring transactions fetched", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringResponse>> updateRecurring(
            @PathVariable Long id,
            @Valid @RequestBody RecurringRequest request,
            HttpServletRequest servletRequest) {
        Long userId = getUserIdFromHeader(servletRequest);
        RecurringResponse response = recurringService.updateRecurring(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction updated", response));
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<ApiResponse<RecurringResponse>> deactivateRecurring(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {
        Long userId = getUserIdFromHeader(servletRequest);
        RecurringResponse response = recurringService.deactivateRecurring(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction deactivated", response));
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<ApiResponse<RecurringResponse>> activateRecurring(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {
        Long userId = getUserIdFromHeader(servletRequest);
        RecurringResponse response = recurringService.activateRecurring(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction activated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecurring(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {
        Long userId = getUserIdFromHeader(servletRequest);
        recurringService.deleteRecurring(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction deleted", null));
    }

    // Endpoint to manually trigger the scheduler logic (for testing)
    @PostMapping("/trigger-scheduler")
    public ResponseEntity<ApiResponse<Void>> triggerScheduler() {
        recurringService.processUpcomingDue();
        return ResponseEntity.ok(ApiResponse.success("Scheduler triggered manually", null));
    }
}
