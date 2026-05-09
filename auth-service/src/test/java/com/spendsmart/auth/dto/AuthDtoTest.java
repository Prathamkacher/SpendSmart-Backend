package com.spendsmart.auth.dto;

import com.spendsmart.auth.dto.admin.PlatformAnalytics;
import com.spendsmart.auth.dto.admin.TopUserDTO;
import com.spendsmart.auth.dto.admin.TransactionDTO;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AuthDtoTest {

    @Test
    void testAuthResponse() {
        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(new UserProfileResponse())
                .build();

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertNotNull(response.toString());
    }

    @Test
    void testLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("pass");
        assertEquals("test@test.com", request.getEmail());
        assertNotNull(request.toString());
    }

    @Test
    void testRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setFullName("Test User");
        request.setPassword("pass");
        assertEquals("test@test.com", request.getEmail());
        assertNotNull(request.toString());
    }

    @Test
    void testRefreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token");
        assertEquals("token", request.getRefreshToken());
        assertNotNull(request.toString());
    }

    @Test
    void testForgotPasswordRequest() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@test.com");
        assertEquals("test@test.com", request.getEmail());
        assertNotNull(request.toString());
    }

    @Test
    void testVerifyOtpRequest() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@test.com");
        request.setOtp("123456");
        assertEquals("123456", request.getOtp());
        assertNotNull(request.toString());
    }

    @Test
    void testResetPasswordRequest() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@test.com");
        request.setOtp("123456");
        request.setNewPassword("newpass");
        assertEquals("newpass", request.getNewPassword());
        assertNotNull(request.toString());
    }

    @Test
    void testChangePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new");
        assertEquals("new", request.getNewPassword());
        assertNotNull(request.toString());
    }

    @Test
    void testUpdateProfileRequest() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Name");
        request.setAvatarUrl("url");
        assertEquals("New Name", request.getFullName());
        assertNotNull(request.toString());
    }

    @Test
    void testProfileUpdateRequest() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName("Name");
        assertEquals("Name", request.getFullName());
        assertNotNull(request.toString());
    }

    @Test
    void testUserProfileResponse() {
        UserProfileResponse response = new UserProfileResponse();
        response.setEmail("test@test.com");
        response.setFullName("User");
        assertEquals("User", response.getFullName());
        assertNotNull(response.toString());
    }

    @Test
    void testTransactionDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setDescription("Trans");
        assertEquals("Trans", dto.getDescription());
        assertNotNull(dto.toString());
    }

    @Test
    void testPlatformAnalytics() {
        PlatformAnalytics analytics = new PlatformAnalytics();
        analytics.setTotalUsers(100L);
        assertEquals(100L, analytics.getTotalUsers());
        assertNotNull(analytics.toString());
    }

    @Test
    void testTopUserDTO() {
        TopUserDTO dto = new TopUserDTO();
        dto.setFullName("Top");
        assertEquals("Top", dto.getFullName());
        assertNotNull(dto.toString());
    }
}
