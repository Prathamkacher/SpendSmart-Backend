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

        AuthResponse response2 = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(new UserProfileResponse())
                .build();

        assertEquals(response, response2);
        assertEquals(response.hashCode(), response2.hashCode());
        
        response2.setAccessToken("diff");
        assertNotEquals(response, response2);
        assertNotNull(response.toString());
        assertNotNull(AuthResponse.builder().toString());
    }

    @Test
    void testLoginRequest() {
        LoginRequest req1 = new LoginRequest(); req1.setEmail("t@t.com"); req1.setPassword("p");
        LoginRequest req2 = new LoginRequest(); req2.setEmail("t@t.com"); req2.setPassword("p");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setEmail("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testRegisterRequest() {
        RegisterRequest req1 = new RegisterRequest(); req1.setEmail("t"); req1.setFullName("n"); req1.setPassword("p");
        RegisterRequest req2 = new RegisterRequest(); req2.setEmail("t"); req2.setFullName("n"); req2.setPassword("p");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setEmail("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testRefreshTokenRequest() {
        RefreshTokenRequest req1 = new RefreshTokenRequest(); req1.setRefreshToken("t");
        RefreshTokenRequest req2 = new RefreshTokenRequest(); req2.setRefreshToken("t");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setRefreshToken("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testForgotPasswordRequest() {
        ForgotPasswordRequest req1 = new ForgotPasswordRequest(); req1.setEmail("t");
        ForgotPasswordRequest req2 = new ForgotPasswordRequest(); req2.setEmail("t");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setEmail("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testVerifyOtpRequest() {
        VerifyOtpRequest req1 = new VerifyOtpRequest(); req1.setEmail("t"); req1.setOtp("o");
        VerifyOtpRequest req2 = new VerifyOtpRequest(); req2.setEmail("t"); req2.setOtp("o");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setEmail("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testResetPasswordRequest() {
        ResetPasswordRequest req1 = new ResetPasswordRequest(); req1.setEmail("t"); req1.setOtp("o"); req1.setNewPassword("n");
        ResetPasswordRequest req2 = new ResetPasswordRequest(); req2.setEmail("t"); req2.setOtp("o"); req2.setNewPassword("n");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setEmail("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testChangePasswordRequest() {
        ChangePasswordRequest req1 = new ChangePasswordRequest(); req1.setCurrentPassword("c"); req1.setNewPassword("n");
        ChangePasswordRequest req2 = new ChangePasswordRequest(); req2.setCurrentPassword("c"); req2.setNewPassword("n");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setCurrentPassword("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testUpdateProfileRequest() {
        UpdateProfileRequest req1 = new UpdateProfileRequest(); req1.setFullName("n"); req1.setAvatarUrl("u");
        UpdateProfileRequest req2 = new UpdateProfileRequest(); req2.setFullName("n"); req2.setAvatarUrl("u");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setFullName("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testProfileUpdateRequest() {
        ProfileUpdateRequest req1 = new ProfileUpdateRequest(); req1.setFullName("n");
        ProfileUpdateRequest req2 = new ProfileUpdateRequest(); req2.setFullName("n");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        req2.setFullName("d"); assertNotEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test
    void testUserProfileResponse() {
        UserProfileResponse res1 = new UserProfileResponse(); res1.setEmail("e"); res1.setFullName("n");
        UserProfileResponse res2 = new UserProfileResponse(); res2.setEmail("e"); res2.setFullName("n");
        assertEquals(res1, res2);
        assertEquals(res1.hashCode(), res2.hashCode());
        res2.setEmail("d"); assertNotEquals(res1, res2);
        assertNotNull(res1.toString());
    }

    @Test
    void testTransactionDTO() {
        TransactionDTO dto1 = new TransactionDTO(); dto1.setDescription("d");
        TransactionDTO dto2 = new TransactionDTO(); dto2.setDescription("d");
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        dto2.setDescription("diff"); assertNotEquals(dto1, dto2);
        assertNotNull(dto1.toString());
    }

    @Test
    void testPlatformAnalytics() {
        PlatformAnalytics pa1 = new PlatformAnalytics(); pa1.setTotalUsers(100L);
        PlatformAnalytics pa2 = new PlatformAnalytics(); pa2.setTotalUsers(100L);
        assertEquals(pa1, pa2);
        assertEquals(pa1.hashCode(), pa2.hashCode());
        pa2.setTotalUsers(200L); assertNotEquals(pa1, pa2);
        assertNotNull(pa1.toString());
    }

    @Test
    void testTopUserDTO() {
        TopUserDTO dto1 = new TopUserDTO(); dto1.setFullName("n");
        TopUserDTO dto2 = new TopUserDTO(); dto2.setFullName("n");
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        dto2.setFullName("d"); assertNotEquals(dto1, dto2);
        assertNotNull(dto1.toString());
    }
}
