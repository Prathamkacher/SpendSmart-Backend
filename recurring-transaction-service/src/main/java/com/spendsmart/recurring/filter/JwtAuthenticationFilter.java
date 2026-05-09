package com.spendsmart.recurring.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter implements Filter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");
        
        // If coming from API Gateway, it might already have X-User-Id
        String existingUserId = httpRequest.getHeader("X-User-Id");

        if (existingUserId != null && !existingUserId.isEmpty()) {
            // Trust the gateway
            chain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                Number userIdNum = claims.get("userId", Number.class);
                String userId = userIdNum != null ? String.valueOf(userIdNum.longValue()) : null;
                
                if (userId == null) {
                    log.error("userId claim is missing from token!");
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                // Create a wrapper to add the X-User-Id header
                HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(httpRequest);
                requestWrapper.addHeader("X-User-Id", userId);
                
                chain.doFilter(requestWrapper, response);
                return;
            } catch (Exception e) {
                log.error("JWT Validation failed: {}", e.getMessage());
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // Allow to proceed, but controller might fail if X-User-Id is required
        chain.doFilter(request, response);
    }

    private static class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
        private Map<String, String> headerMap = new HashMap<>();

        public HeaderMapRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public void addHeader(String name, String value) {
            headerMap.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = super.getHeader(name);
            if (headerMap.containsKey(name)) {
                headerValue = headerMap.get(name);
            }
            return headerValue;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            names.addAll(headerMap.keySet());
            return Collections.enumeration(names);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> values = Collections.list(super.getHeaders(name));
            if (headerMap.containsKey(name)) {
                values.add(headerMap.get(name));
            }
            return Collections.enumeration(values);
        }
    }
}
