package com.spendsmart.budget.config;

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
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, configuredObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_ShouldPopulateAuthenticationForValidToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, configuredObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail("valid-token")).thenReturn("budget@example.com");
        when(jwtService.isTokenValid("valid-token", "budget@example.com")).thenReturn(true);
        when(jwtService.extractUserId("valid-token")).thenReturn(12L);
        when(jwtService.extractRole("valid-token")).thenReturn("ROLE_ADMIN");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(request.getAttribute("userId")).isEqualTo(12L);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorizedJsonWhenJwtFails() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, configuredObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer broken-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail("broken-token")).thenThrow(new RuntimeException("token broken"));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("token broken");
    }

    private ObjectMapper configuredObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
