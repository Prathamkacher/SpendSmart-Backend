package com.spendsmart.auth.resource;

import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.dto.admin.PlatformAnalytics;
import com.spendsmart.auth.dto.admin.TopUserDTO;
import com.spendsmart.auth.dto.admin.TransactionDTO;
import com.spendsmart.auth.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Panel", description = "Management APIs for SpendSmart Platform")
@PreAuthorize("hasRole('ADMIN')")
public class AdminResource {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Get all registered users")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", adminService.getAllUsers()));
    }

    @PutMapping("/users/{id}/suspend")
    @Operation(summary = "Suspend a user account")
    public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable Long id) {
        adminService.suspendUser(id);
        return ResponseEntity.ok(ApiResponse.success("User suspended successfully"));
    }

    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Reactivate a user account")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        adminService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully"));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all global transactions (Expenses + Incomes)")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched", adminService.getAllTransactions()));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get platform-wide analytics")
    public ResponseEntity<ApiResponse<PlatformAnalytics>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.success("Analytics fetched", adminService.getPlatformAnalytics()));
    }

    @GetMapping("/top-users")
    @Operation(summary = "Get top spending users")
    public ResponseEntity<ApiResponse<List<TopUserDTO>>> getTopUsers() {
        return ResponseEntity.ok(ApiResponse.success("Top users fetched", adminService.getTopSpendingUsers()));
    }

    @PostMapping("/notify")
    @Operation(summary = "Send a global system notification")
    public ResponseEntity<ApiResponse<Void>> notifyUsers(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String message = payload.get("message");
        String severity = payload.getOrDefault("severity", "INFO");
        adminService.sendGlobalNotification(title, message, severity);
        return ResponseEntity.ok(ApiResponse.success("Notification sent to all users"));
    }

    @GetMapping("/report")
    @Operation(summary = "Export platform report")
    public ResponseEntity<byte[]> exportReport() {
        byte[] report = adminService.exportPlatformReport();
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("Content-Disposition", "attachment; filename=platform_report.json")
                .body(report);
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role (Promote/Demote)")
    public ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable Long id, @RequestParam String role) {
        adminService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated to " + role));
    }
}
