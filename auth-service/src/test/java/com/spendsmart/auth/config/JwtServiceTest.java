package com.spendsmart.auth.config;

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
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void generateToken_ShouldExposeClaims() {
        String token = jwtService.generateToken("user@example.com", 42L, "USER", "PRO");

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
        assertThat(jwtService.extractPlanType(token)).isEqualTo("PRO");
        assertThat(jwtService.isTokenValid(token, "user@example.com")).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForExpiredToken() {
        String token = buildToken("user@example.com", new Date(System.currentTimeMillis() - 1000));

        assertThat(jwtService.isTokenValid(token, "user@example.com")).isFalse();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("bad-token", "user@example.com")).isFalse();
    }

    private String buildToken(String subject, Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .subject(subject)
                .claim("userId", 7L)
                .claim("role", "USER")
                .claim("planType", "FREE")
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}
