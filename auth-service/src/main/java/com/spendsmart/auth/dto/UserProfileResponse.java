// com/spendsmart/auth/dto/UserProfileResponse.java
package com.spendsmart.auth.dto;

import com.spendsmart.auth.entity.User;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for detailed user profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long          userId;
    private String        fullName;
    private String        email;
    private String        currency;
    private String        timezone;
    private String        avatarUrl;
    private String        firstInitial;
    private String        bio;
    private User.AuthProvider provider;
    private User.Role     role;
    private Boolean       isActive;
    private BigDecimal    monthlyBudget;
    private User.PlanType planType;
    private LocalDateTime planStartDate;
    private LocalDateTime planExpiryDate;
    private Boolean       isTrialUsed;
    private LocalDateTime createdAt;
}