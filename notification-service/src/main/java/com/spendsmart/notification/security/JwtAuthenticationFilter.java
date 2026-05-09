package com.spendsmart.notification.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.shared.security.AbstractJwtAuthenticationFilter;
import com.spendsmart.shared.security.JwtClaimsAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    protected String authorityForRole(String role) {
        return role != null ? role : "ROLE_USER";
    }

    @Override
    protected void logFailure(HttpServletRequest request, Exception exception) {
    }
}
