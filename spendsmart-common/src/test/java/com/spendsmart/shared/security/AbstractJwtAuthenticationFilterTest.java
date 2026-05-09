package com.spendsmart.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractJwtAuthenticationFilterTest {

    private JwtClaimsAccessor jwtClaimsAccessor;
    private ObjectMapper objectMapper;
    private TestJwtAuthenticationFilter filter;
    private LenientJwtAuthenticationFilter lenientFilter;
    private NullMessageJwtAuthenticationFilter nullMessageFilter;

    private static class TestJwtAuthenticationFilter extends AbstractJwtAuthenticationFilter {
        private final JwtClaimsAccessor jwtClaimsAccessor;
        private final ObjectMapper objectMapper;

        public TestJwtAuthenticationFilter(JwtClaimsAccessor jwtClaimsAccessor, ObjectMapper objectMapper) {
            this.jwtClaimsAccessor = jwtClaimsAccessor;
            this.objectMapper = objectMapper;
        }

        @Override
        protected JwtClaimsAccessor jwtClaimsAccessor() {
            return jwtClaimsAccessor;
        }

        @Override
        protected ObjectMapper objectMapper() {
            return objectMapper;
        }
    }

    private static class LenientJwtAuthenticationFilter extends TestJwtAuthenticationFilter {
        private Exception loggedException;

        public LenientJwtAuthenticationFilter(JwtClaimsAccessor jwtClaimsAccessor, ObjectMapper objectMapper) {
            super(jwtClaimsAccessor, objectMapper);
        }

        @Override
        protected boolean continueOnFailure() {
            return true;
        }

        @Override
        protected void logFailure(HttpServletRequest request, Exception exception) {
            this.loggedException = exception;
        }
    }

    private static class NullMessageJwtAuthenticationFilter extends TestJwtAuthenticationFilter {
        public NullMessageJwtAuthenticationFilter(JwtClaimsAccessor jwtClaimsAccessor, ObjectMapper objectMapper) {
            super(jwtClaimsAccessor, objectMapper);
        }

        @Override
        protected String invalidTokenMessage() {
            return "Fallback message";
        }
    }

    @BeforeEach
    void setUp() {
        jwtClaimsAccessor = mock(JwtClaimsAccessor.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        filter = new TestJwtAuthenticationFilter(jwtClaimsAccessor, objectMapper);
        lenientFilter = new LenientJwtAuthenticationFilter(jwtClaimsAccessor, objectMapper);
        nullMessageFilter = new NullMessageJwtAuthenticationFilter(jwtClaimsAccessor, objectMapper);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testMissingAuthHeader() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testWrongPrefix() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Basic abcd");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testValidToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        String jwt = "valid.jwt.token";
        String email = "user@test.com";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtClaimsAccessor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimsAccessor.isTokenValid(jwt, email)).thenReturn(true);
        when(jwtClaimsAccessor.extractUserId(jwt)).thenReturn(1L);
        when(jwtClaimsAccessor.extractRole(jwt)).thenReturn("USER");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(request).setAttribute("userId", 1L);
    }

    @Test
    void testDoesNotOverrideExistingAuthentication() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtClaimsAccessor.extractEmail("valid.jwt.token")).thenReturn("user@test.com");

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "existing@test.com", null, java.util.List.of()));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtClaimsAccessor, never()).isTokenValid(anyString(), anyString());
        assertEquals("existing@test.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void testInvalidToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        String jwt = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtClaimsAccessor.extractEmail(jwt)).thenThrow(new RuntimeException("Invalid"));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("Invalid"));
    }

    @Test
    void testInvalidTokenFallsBackToDefaultMessageWhenExceptionMessageMissing() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(jwtClaimsAccessor.extractEmail("invalid.token")).thenThrow(new RuntimeException());

        nullMessageFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("Fallback message"));
    }

    @Test
    void testContinueOnFailurePassesRequestDownstream() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        RuntimeException failure = new RuntimeException("boom");
        when(jwtClaimsAccessor.extractEmail("invalid.token")).thenThrow(failure);

        lenientFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertSame(failure, lenientFilter.loggedException);
    }
}
