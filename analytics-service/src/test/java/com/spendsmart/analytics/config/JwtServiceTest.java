package com.spendsmart.analytics.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "12345678901234567890123456789012";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
    }

    @Test
    void extractors_ShouldReadClaimsFromValidToken() {
        String token = buildToken("analytics@example.com", 5L, new Date(System.currentTimeMillis() + 60_000));

        assertThat(jwtService.extractEmail(token)).isEqualTo("analytics@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(5L);
        assertThat(jwtService.isTokenValid(token, "analytics@example.com")).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForExpiredOrBadToken() {
        String expiredToken = buildToken("analytics@example.com", 5L, new Date(System.currentTimeMillis() - 1_000));

        assertThat(jwtService.isTokenValid(expiredToken, "analytics@example.com")).isFalse();
        assertThat(jwtService.isTokenValid("not-a-token", "analytics@example.com")).isFalse();
    }

    private String buildToken(String subject, Long userId, Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId)
                .issuedAt(new Date(System.currentTimeMillis() - 2_000))
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}
