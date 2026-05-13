package com.spendsmart.analytics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.shared.security.AbstractJwtAuthenticationFilter;
import com.spendsmart.shared.security.JwtClaimsAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Filter that intercepts HTTP requests to validate JWT tokens.
 * Extends {@link AbstractJwtAuthenticationFilter} for standardized token processing in analytics service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends AbstractJwtAuthenticationFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected JwtClaimsAccessor jwtClaimsAccessor() {
        return jwtService;
    }

    @Override
    protected ObjectMapper objectMapper() {
        return objectMapper;
    }

    @Override
    protected void logFailure(HttpServletRequest request, Exception exception) {
        log.error("JWT filter error: {}", exception.getMessage());
    }
}
