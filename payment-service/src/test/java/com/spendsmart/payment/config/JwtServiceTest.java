package com.spendsmart.payment.config;

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
    void extractors_ShouldSupportIntegerUserIdClaims() {
        String token = buildToken("pay@example.com", 7, "ADMIN", new Date(System.currentTimeMillis() + 60000));

        assertThat(jwtService.extractEmail(token)).isEqualTo("pay@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(7L);
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwtService.isTokenValid(token, "pay@example.com")).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForExpiredOrMalformedToken() {
        String expired = buildToken("pay@example.com", 7L, "USER", new Date(System.currentTimeMillis() - 1000));

        assertThat(jwtService.isTokenValid(expired, "pay@example.com")).isFalse();
        assertThat(jwtService.isTokenValid("bad-token", "pay@example.com")).isFalse();
    }

    private String buildToken(String subject, Object userId, String role, Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}
