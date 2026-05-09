package com.spendsmart.auth.resource;

import com.spendsmart.auth.constants.AppConstants;
import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.service.AuthService;
import com.spendsmart.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = AppConstants.SWAGGER_TAG_AUTH, description = "Register, Login, Logout, Token Refresh")
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.REGISTER_SUCCESS, response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.LOGIN_SUCCESS, response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get a new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(AppConstants.TOKEN_REFRESH_SUCCESS, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh tokens",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        Long userId = extractUserId(request);
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.LOGOUT_SUCCESS, null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset OTP")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("If the email is registered, an OTP has been sent.", null));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify the 6-digit OTP")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success("OTP is valid.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Set a new password using OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully.", null));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile",
               security = @SecurityRequirement(name = "BearerAuth"))
    @Tag(name = AppConstants.SWAGGER_TAG_PROFILE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(HttpServletRequest request) {
        Long userId = extractUserId(request);
        UserProfileResponse profile = authService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile",
               security = @SecurityRequirement(name = "BearerAuth"))
    @Tag(name = AppConstants.SWAGGER_TAG_PROFILE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest updateRequest) {
        Long userId = extractUserId(request);
        UserProfileResponse updated = authService.updateProfile(userId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.PROFILE_UPDATE_SUCCESS, updated));
    }

    @PutMapping("/profile/upgrade")
    @Operation(summary = "Upgrade user plan (internal use)")
    public ResponseEntity<ApiResponse<Void>> upgradeProfile(
            @RequestParam Long userId,
            @RequestParam String planType,
            @RequestParam Integer durationMonths) {
        authService.upgradeUserPlan(userId, planType, durationMonths);
        return ResponseEntity.ok(ApiResponse.success("Plan upgraded successfully", null));
    }

    @PutMapping("/internal/promote-admin")
    @Operation(summary = "Promote a user to ADMIN (internal use)")
    public ResponseEntity<ApiResponse<Void>> promoteToAdmin(@RequestParam String email) {
        authService.promoteToAdmin(email);
        return ResponseEntity.ok(ApiResponse.success("User promoted to ADMIN", null));
    }

    @PutMapping("/password")
    @Operation(summary = "Change account password",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody ChangePasswordRequest changeRequest) {
        Long userId = extractUserId(request);
        authService.changePassword(userId, changeRequest);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.PASSWORD_CHANGE_SUCCESS));
    }

    @PatchMapping("/currency")
    @Operation(summary = "Update preferred display currency",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateCurrency(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        Long userId = extractUserId(request);
        authService.updateCurrency(userId, body.get("currency"));
        return ResponseEntity.ok(ApiResponse.success(AppConstants.CURRENCY_UPDATE_SUCCESS));
    }

    @DeleteMapping("/deactivate")
    @Operation(summary = "Soft-delete / deactivate account",
               security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deactivate(HttpServletRequest request) {
        Long userId = extractUserId(request);
        authService.deactivateAccount(userId);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.ACCOUNT_DEACTIVATE_SUCCESS));
    }

    @GetMapping("/users/ids")
    @Operation(summary = "Get all user IDs (internal use)")
    public ResponseEntity<ApiResponse<List<Long>>> getAllUserIds() {
        return ResponseEntity.ok(ApiResponse.success("User IDs fetched", authService.getAllUserIds()));
    }

    @GetMapping("/internal/users/{id}")
    @Operation(summary = "Get user profile by ID (internal use)")
    public ResponseEntity<UserProfileResponse> getUserInternal(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    private Long extractUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("UserId missing from request attributes in Auth Service!");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        if (userIdObj instanceof Integer integer) {
            return integer.longValue();
        }
        return (Long) userIdObj;
    }
}
