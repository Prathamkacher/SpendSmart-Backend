package com.spendsmart.income.config;

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
        String token = buildToken("income@example.com", 5L, "USER", new Date(System.currentTimeMillis() + 60_000));

        assertThat(jwtService.extractEmail(token)).isEqualTo("income@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(5L);
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
        assertThat(jwtService.isTokenValid(token, "income@example.com")).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForExpiredOrBadToken() {
        String expiredToken = buildToken("income@example.com", 5L, "USER", new Date(System.currentTimeMillis() - 1_000));

        assertThat(jwtService.isTokenValid(expiredToken, "income@example.com")).isFalse();
        assertThat(jwtService.isTokenValid("not-a-token", "income@example.com")).isFalse();
    }

    private String buildToken(String subject, Long userId, String role, Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis() - 2_000))
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}
