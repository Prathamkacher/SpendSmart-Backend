package com.spendsmart.shared.security;

public interface JwtClaimsAccessor {

    String extractEmail(String token);

    Long extractUserId(String token);

    String extractRole(String token);

    boolean isTokenValid(String token, String email);
}
