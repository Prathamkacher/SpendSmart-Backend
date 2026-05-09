package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;
import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(Long userId);

    AuthResponse refreshToken(String refreshToken);

    UserProfileResponse getUserById(Long userId);

    UserProfileResponse getUserByEmail(String email);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void updateCurrency(Long userId, String currency);

    void deactivateAccount(Long userId);

    List<Long> getAllUserIds();
    
    // Subscription logic
    void upgradeUserPlan(Long userId, String planType, Integer durationMonths);

    void promoteToAdmin(String email);
    
    // Forgot Password Flow
    void forgotPassword(String email);
    
    void verifyOtp(String email, String otp);
    
    void resetPassword(String email, String otp, String newPassword);
}
