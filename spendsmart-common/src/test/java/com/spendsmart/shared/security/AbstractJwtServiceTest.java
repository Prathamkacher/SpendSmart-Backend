package com.spendsmart.shared.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbstractJwtServiceTest {

    private static final String SECRET = "verysecretkeythatisatleast32characterslong!!!";
    private TestJwtService jwtService;

    private static class TestJwtService extends AbstractJwtService {
        private final List<Exception> validationFailures = new ArrayList<>();
        private final List<JwtException> parseFailures = new ArrayList<>();

        @Override
        protected String getJwtSecret() {
            return SECRET;
        }

        @Override
        protected void onValidationFailure(Exception exception) {
            validationFailures.add(exception);
        }

        @Override
        protected void onParseFailure(JwtException exception) {
            parseFailures.add(exception);
        }
    }

    @BeforeEach
    void setUp() {
        jwtService = new TestJwtService();
    }

    private String generateToken(String email, Long userId, String role, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Test
    void testExtractClaims() {
        String email = "test@example.com";
        Long userId = 123L;
        String role = "ADMIN";
        String token = generateToken(email, userId, role, 3600000);

        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void testIsTokenValid() {
        String email = "test@example.com";
        String token = generateToken(email, 123L, "USER", 3600000);

        assertTrue(jwtService.isTokenValid(token, email));
        assertFalse(jwtService.isTokenValid(token, "wrong@example.com"));
        assertTrue(jwtService.validationFailures.isEmpty());
    }

    @Test
    void testIsTokenExpired() {
        // Token expired 1 hour ago
        String token = generateToken("test@example.com", 123L, "USER", -3600000);
        
        // isTokenExpired throws ExpiredJwtException because JJWT rejects expired tokens during parsing
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenExpired(token));
        // isTokenValid catches the exception internally and returns false
        assertFalse(jwtService.isTokenValid(token, "test@example.com"));
        assertFalse(jwtService.validationFailures.isEmpty());
        assertFalse(jwtService.parseFailures.isEmpty());
    }

    @Test
    void testToLongHandling() {
        // Test different types of userId in claims
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        
        // As Integer
        String tokenInt = Jwts.builder()
                .claim("userId", 456)
                .subject("test@example.com")
                .signWith(key)
                .compact();
        assertEquals(456L, jwtService.extractUserId(tokenInt));

        // As String
        String tokenStr = Jwts.builder()
                .claim("userId", "789")
                .subject("test@example.com")
                .signWith(key)
                .compact();
        assertEquals(789L, jwtService.extractUserId(tokenStr));
        
        // As Null
        String tokenNull = Jwts.builder()
                .subject("test@example.com")
                .signWith(key)
                .compact();
        assertNull(jwtService.extractUserId(tokenNull));
    }

    @Test
    void testInvalidTokenTriggersParseAndValidationHooks() {
        String invalidToken = "not-a-jwt";

        assertThrows(JwtException.class, () -> jwtService.extractEmail(invalidToken));
        assertFalse(jwtService.parseFailures.isEmpty());

        int parseFailureCount = jwtService.parseFailures.size();
        assertFalse(jwtService.isTokenValid(invalidToken, "test@example.com"));
        assertFalse(jwtService.validationFailures.isEmpty());
        assertTrue(jwtService.parseFailures.size() >= parseFailureCount);
    }
}
