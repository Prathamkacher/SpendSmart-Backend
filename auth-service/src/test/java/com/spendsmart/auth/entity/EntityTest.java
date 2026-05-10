package com.spendsmart.auth.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testUser() {
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setUserId(1L);
        user.setFullName("Test User");
        user.setEmail("test@test.com");
        user.setPasswordHash("hash");
        user.setCurrency("INR");
        user.setTimezone("Asia/Kolkata");
        user.setAvatarUrl("url");
        user.setBio("bio");
        user.setProvider(User.AuthProvider.LOCAL);
        user.setIsActive(true);
        user.setMonthlyBudget(BigDecimal.ZERO);
        user.setRole(User.Role.USER);
        user.setResetOtp("123456");
        user.setResetOtpExpiry(now);
        user.setPlanType(User.PlanType.FREE);
        user.setPlanStartDate(now);
        user.setPlanExpiryDate(now);
        user.setIsTrialUsed(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(1L, user.getUserId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals(User.Role.USER, user.getRole());
        assertTrue(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        
        User user2 = new User(1L, "Test User", "test@test.com", "hash", "INR", "Asia/Kolkata", "url", "bio", User.AuthProvider.LOCAL, true, BigDecimal.ZERO, User.Role.USER, "123456", now, User.PlanType.FREE, now, now, false, now, now);
        assertEquals(user, user2);
        assertEquals(user.hashCode(), user2.hashCode());
        
        User user3 = new User();
        user3.setUserId(2L);
        assertNotEquals(user, user3);
        
        User user4 = User.builder()
            .userId(1L)
            .fullName("Test User")
            .email("test@test.com")
            .passwordHash("hash")
            .currency("INR")
            .timezone("Asia/Kolkata")
            .avatarUrl("url")
            .bio("bio")
            .provider(User.AuthProvider.LOCAL)
            .isActive(true)
            .monthlyBudget(BigDecimal.ZERO)
            .role(User.Role.USER)
            .resetOtp("123456")
            .resetOtpExpiry(now)
            .planType(User.PlanType.FREE)
            .planStartDate(now)
            .planExpiryDate(now)
            .isTrialUsed(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
            
        assertEquals(user, user4);
        assertNotNull(user.toString());
        assertNotNull(User.builder().toString());
    }

    @Test
    void testRefreshToken() {
        LocalDateTime now = LocalDateTime.now();

        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setUser(new User());
        token.setToken("token");
        token.setExpiryDate(now.plusDays(1));
        token.setIsRevoked(false);

        assertEquals(1L, token.getId());
        assertEquals("token", token.getToken());
        assertFalse(token.getIsRevoked());
        
        RefreshToken token2 = new RefreshToken();
        token2.setId(1L);
        token2.setUser(new User());
        token2.setToken("token");
        token2.setExpiryDate(now.plusDays(1));
        token2.setIsRevoked(false);
        
        assertEquals(token, token2);
        assertEquals(token.hashCode(), token2.hashCode());
        
        RefreshToken token3 = new RefreshToken();
        token3.setId(2L);
        assertNotEquals(token, token3);
        
        assertNotNull(token.toString());
    }
}
