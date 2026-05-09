package com.spendsmart.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that makes the CSRF token available to the client.
 * Spring Security 6 uses deferred CSRF tokens, so we need to access the token
 * to ensure it's generated and sent to the client in a cookie.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Render the token value to a cookie by causing the deferred token to be loaded
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
