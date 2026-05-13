package com.spendsmart.category.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.shared.security.AbstractJwtAuthenticationFilter;
import com.spendsmart.shared.security.JwtClaimsAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Filter that intercepts incoming HTTP requests to validate JWT tokens.
 * Extends {@link AbstractJwtAuthenticationFilter} to provide microservice-specific
 * implementation for JWT claims access and logging.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends AbstractJwtAuthenticationFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    /**
     * Provides the JWT claims accessor (service) used for token validation.
     * @return The JwtService implementation.
     */
    @Override
    protected JwtClaimsAccessor jwtClaimsAccessor() {
        return jwtService;
    }

    /**
     * Provides the ObjectMapper for serializing error responses.
     * @return The Jackson ObjectMapper.
     */
    @Override
    protected ObjectMapper objectMapper() {
        return objectMapper;
    }

    /**
     * Logs JWT authentication failures.
     * @param request The current HTTP request.
     * @param exception The exception that caused the failure.
     */
    @Override
    protected void logFailure(HttpServletRequest request, Exception exception) {
        log.error("JWT filter error: {}", exception.getMessage());
    }
}
