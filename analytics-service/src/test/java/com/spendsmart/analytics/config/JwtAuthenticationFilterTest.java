package com.spendsmart.analytics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldPassThroughWithoutBearerHeader() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_ShouldPopulateAuthenticationForValidToken() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail("valid-token")).thenReturn("analytics@example.com");
        when(jwtService.extractUserId("valid-token")).thenReturn(12L);
        when(jwtService.isTokenValid("valid-token", "analytics@example.com")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(request.getAttribute("userId")).isEqualTo(12L);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorizedOnJwtErrors() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer broken-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail("broken-token")).thenThrow(new RuntimeException("broken"));

        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(filterChain);
        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_UNAUTHORIZED);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
