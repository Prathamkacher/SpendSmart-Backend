package com.spendsmart.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.auth.constants.AppConstants;
import com.spendsmart.shared.security.AbstractJwtAuthenticationFilter;
import com.spendsmart.shared.security.JwtClaimsAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    protected String authHeaderName() {
        return AppConstants.AUTH_HEADER;
    }

    @Override
    protected String bearerPrefix() {
        return AppConstants.BEARER_PREFIX;
    }

    @Override
    protected String authorityForRole(String role) {
        return "ROLE_" + (role != null ? role : AppConstants.ROLE_USER);
    }

    @Override
    protected String invalidTokenMessage() {
        return AppConstants.TOKEN_INVALID;
    }

    @Override
    protected void logFailure(HttpServletRequest request, Exception exception) {
        log.error("JWT filter error for request {}: {}", request.getRequestURI(), exception.getMessage());
    }
}
