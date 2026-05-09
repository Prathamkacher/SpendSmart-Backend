package com.spendsmart.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.shared.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public abstract class AbstractJwtAuthenticationFilter extends OncePerRequestFilter {

    protected abstract JwtClaimsAccessor jwtClaimsAccessor();

    protected abstract ObjectMapper objectMapper();

    protected String authHeaderName() {
        return "Authorization";
    }

    protected String bearerPrefix() {
        return "Bearer ";
    }

    protected String authorityForRole(String role) {
        return role != null ? role : "ROLE_USER";
    }

    protected String invalidTokenMessage() {
        return "Token invalid";
    }

    protected boolean continueOnFailure() {
        return false;
    }

    protected void logFailure(HttpServletRequest request, Exception exception) {
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(authHeaderName());

        if (authHeader == null || !authHeader.startsWith(bearerPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(bearerPrefix().length());

        try {
            final String userEmail = jwtClaimsAccessor().extractEmail(jwt);

            if (userEmail != null
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtClaimsAccessor().isTokenValid(jwt, userEmail)) {
                Long userId = jwtClaimsAccessor().extractUserId(jwt);
                String role = jwtClaimsAccessor().extractRole(jwt);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        List.of(new SimpleGrantedAuthority(authorityForRole(role)))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                request.setAttribute("userId", userId);
            }
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            logFailure(request, exception);
            if (continueOnFailure()) {
                filterChain.doFilter(request, response);
                return;
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            String message = exception.getMessage() != null ? exception.getMessage() : invalidTokenMessage();
            response.getWriter().write(objectMapper().writeValueAsString(ApiResponse.error(message)));
        }
    }
}
