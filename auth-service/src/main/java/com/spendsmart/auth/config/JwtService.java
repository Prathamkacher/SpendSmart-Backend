package com.spendsmart.auth.config;

import com.spendsmart.shared.security.AbstractJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing JSON Web Tokens.
 * Handles generation, extraction of custom claims, and validation.
 */
@Slf4j
@Component
public class JwtService extends AbstractJwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Generates a new JWT for a user with specific claims.
     *
     * @param email The user's email (subject).
     * @param userId The unique user ID.
     * @param role The user's role (e.g., ADMIN, USER).
     * @param planType The user's subscription plan.
     * @return A signed JWT string.
     */
    public String generateToken(String email, Long userId, String role, String planType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("planType", planType);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractPlanType(String token) {
        return extractClaim(token, claims -> claims.get("planType", String.class));
    }

    @Override
    protected void onValidationFailure(Exception exception) {
        if (exception instanceof JwtException jwtException) {
            log.warn("JWT validation failed: {}", jwtException.getMessage());
        }
    }

    @Override
    protected void onParseFailure(JwtException exception) {
        log.error("Failed to parse JWT: {}", exception.getMessage());
    }

    @Override
    protected String getJwtSecret() {
        return jwtSecret;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
