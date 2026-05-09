package com.spendsmart.auth.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.config.JwtService;
import com.spendsmart.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthResource.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password@123");
        request.setFullName("Test User");

        when(authService.register(any())).thenReturn(new AuthResponse());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void login_ShouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authService.login(any())).thenReturn(new AuthResponse());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_ShouldReturn200() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token");

        when(authService.refreshToken(anyString())).thenReturn(new AuthResponse());

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void logout_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_ShouldReturn200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_ShouldReturn200() throws Exception {
        when(authService.getUserById(anyLong())).thenReturn(new UserProfileResponse());

        mockMvc.perform(get("/auth/profile")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfile_ShouldReturn200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Name");

        when(authService.updateProfile(anyLong(), any())).thenReturn(new UserProfileResponse());

        mockMvc.perform(put("/auth/profile")
                .requestAttr("userId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCurrency_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/auth/currency")
                .requestAttr("userId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currency\":\"USD\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void upgradeProfile_ShouldReturn200() throws Exception {
        mockMvc.perform(put("/auth/profile/upgrade")
                .param("userId", "1")
                .param("planType", "PRO")
                .param("durationMonths", "12"))
                .andExpect(status().isOk());
    }

    @Test
    void promoteToAdmin_ShouldReturn200() throws Exception {
        mockMvc.perform(put("/auth/internal/promote-admin")
                .param("email", "test@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivate_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/auth/deactivate")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_WithoutUserId_ShouldReturn500() throws Exception {
        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllUserIds_ShouldReturn200() throws Exception {
        when(authService.getAllUserIds()).thenReturn(Collections.singletonList(1L));

        mockMvc.perform(get("/auth/users/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(1L));
    }

    @Test
    void getUserInternal_ShouldReturn200() throws Exception {
        when(authService.getUserById(1L)).thenReturn(new UserProfileResponse());

        mockMvc.perform(get("/auth/internal/users/1"))
                .andExpect(status().isOk());
    }
}
