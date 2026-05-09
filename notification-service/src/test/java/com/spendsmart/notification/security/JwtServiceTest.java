package com.spendsmart.notification.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "12345678901234567890123456789012";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
    }

    @Test
    void extractors_ShouldReadClaimsFromValidToken() {
        String token = buildToken("notify@example.com", 5L, new Date(System.currentTimeMillis() + 60_000));

        assertThat(jwtService.extractUsername(token)).isEqualTo("notify@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(5L);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_ShouldThrowForExpiredToken() {
        String expiredToken = buildToken("notify@example.com", 5L, new Date(System.currentTimeMillis() - 1_000));

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken))
                .isInstanceOf(RuntimeException.class);
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
