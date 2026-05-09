package com.spendsmart.auth.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testUser() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setUserId(1L);
        user.setFullName("Test User");
        user.setEmail("test@test.com");
        user.setPasswordHash("hash");
        user.setRole(User.Role.USER);
        user.setProvider(User.AuthProvider.LOCAL);
        user.setIsActive(true);
        user.setPlanType(User.PlanType.FREE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(1L, user.getUserId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals(User.Role.USER, user.getRole());
        assertTrue(user.getIsActive());
        assertEquals(now, user.getCreatedAt());

        assertNotNull(user.toString());
    }

    @Test
    void testRefreshToken() {
        RefreshToken token = new RefreshToken();
        LocalDateTime now = LocalDateTime.now();

        token.setId(1L);
        token.setUser(new User());
        token.setToken("token");
        token.setExpiryDate(now.plusDays(1));
        token.setIsRevoked(false);

        assertEquals(1L, token.getId());
        assertEquals("token", token.getToken());
        assertFalse(token.getIsRevoked());
        assertNotNull(token.toString());
    }
}
