package com.spendsmart.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.entity.RefreshToken;
import com.spendsmart.auth.mapper.UserMapper;
import com.spendsmart.auth.repository.RefreshTokenRepository;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OAuth2LoginSuccessHandlerTest {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtService jwtService;
    private UserMapper userMapper;
    private ObjectMapper objectMapper;
    private EmailService emailService;
    private OAuth2LoginSuccessHandler handler;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        jwtService = mock(JwtService.class);
        userMapper = mock(UserMapper.class);
        objectMapper = new ObjectMapper();
        emailService = mock(EmailService.class);
        
        handler = new OAuth2LoginSuccessHandler(
                userRepository,
                refreshTokenRepository,
                jwtService,
                userMapper,
                objectMapper,
                emailService
        );
        
        ReflectionTestUtils.setField(handler, "refreshExpiration", 3600000L);
        ReflectionTestUtils.setField(handler, "jwtExpiration", 1800000L);
        ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:4200");
    }

    @Test
    void testOnAuthenticationSuccess_Google() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2User oauth2User = mock(OAuth2User.class);

        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of(
                "email", "test@google.com",
                "name", "Google User",
                "picture", "http://pic.url"
        ));

        User user = User.builder()
                .userId(1L)
                .email("test@google.com")
                .fullName("Google User")
                .isActive(true)
                .role(User.Role.USER)
                .planType(User.PlanType.FREE)
                .build();

        when(userRepository.existsByEmail("test@google.com")).thenReturn(true);
        when(userRepository.findByEmail("test@google.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyString(), anyLong(), anyString(), anyString())).thenReturn("access.token");
        when(userMapper.toProfileResponse(user)).thenReturn(new UserProfileResponse());

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectUrlCaptor.capture());
        
        String redirectUrl = redirectUrlCaptor.getValue();
        assertTrue(redirectUrl.startsWith("http://localhost:4200/auth/callback?session="));
        verify(refreshTokenRepository).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void testOnAuthenticationSuccess_DeactivatedUser() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2User oauth2User = mock(OAuth2User.class);

        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of("email", "inactive@test.com"));

        User user = User.builder()
                .email("inactive@test.com")
                .isActive(false)
                .build();

        when(userRepository.existsByEmail("inactive@test.com")).thenReturn(true);
        when(userRepository.findByEmail("inactive@test.com")).thenReturn(Optional.of(user));

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("http://localhost:4200/auth/login?error=account_deactivated");
    }

    @Test
    void testOnAuthenticationSuccess_UnknownProvider() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2User oauth2User = mock(OAuth2User.class);

        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("facebook");
        when(authentication.getPrincipal()).thenReturn(oauth2User);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("http://localhost:4200/auth/login?error=unknown_provider");
    }
}
