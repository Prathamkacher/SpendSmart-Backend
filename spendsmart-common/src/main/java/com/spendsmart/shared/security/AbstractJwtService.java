package com.spendsmart.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

public abstract class AbstractJwtService implements JwtClaimsAccessor {

    protected abstract String getJwtSecret();

    protected void onValidationFailure(Exception exception) {
    }

    protected void onParseFailure(JwtException exception) {
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> toLong(claims.get("userId")));
    }

    @Override
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public boolean isTokenValid(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            return tokenEmail != null && tokenEmail.equals(email) && !isTokenExpired(token);
        } catch (Exception exception) {
            onValidationFailure(exception);
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    protected Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    protected Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException exception) {
            onParseFailure(exception);
            throw exception;
        }
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(getJwtSecret().getBytes());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }
}
