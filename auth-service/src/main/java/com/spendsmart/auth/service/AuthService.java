package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;
import java.util.List;

/**
 * Service interface for authentication and user management.
 * Defines the contract for user registration, login, profile management, and password recovery.
 */
public interface AuthService {

    /**
     * Registers a new user.
     * @param request The registration details.
     * @return AuthResponse with security tokens.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user.
     * @param request The login credentials.
     * @return AuthResponse with security tokens.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Logs out a user.
     * @param userId The ID of the user.
     */
    void logout(Long userId);

    /**
     * Refreshes the access token using a refresh token.
     * @param refreshToken The refresh token string.
     * @return AuthResponse with new tokens.
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Retrieves a user profile by ID.
     * @param userId The ID of the user.
     * @return UserProfileResponse details.
     */
    UserProfileResponse getUserById(Long userId);

    /**
     * Retrieves a user profile by email.
     * @param email The user's email.
     * @return UserProfileResponse details.
     */
    UserProfileResponse getUserByEmail(String email);

    /**
     * Updates user profile information.
     * @param userId The ID of the user.
     * @param request The updated profile details.
     * @return UserProfileResponse with updated details.
     */
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * Changes a user's password.
     * @param userId The ID of the user.
     * @param request Current and new passwords.
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * Updates the user's preferred currency.
     * @param userId The ID of the user.
     * @param currency The currency code.
     */
    void updateCurrency(Long userId, String currency);

    /**
     * Deactivates a user's account.
     * @param userId The ID of the user.
     */
    void deactivateAccount(Long userId);

    /**
     * Retrieves all user IDs.
     * @return List of user IDs.
     */
    List<Long> getAllUserIds();
    
    /**
     * Upgrades a user's plan.
     * @param userId The ID of the user.
     * @param planType The new plan type.
     * @param durationMonths The duration in months.
     */
    void upgradeUserPlan(Long userId, String planType, Integer durationMonths);

    /**
     * Promotes a user to ADMIN.
     * @param email The email of the user.
     */
    void promoteToAdmin(String email);
    
    /**
     * Initiates the forgot password flow.
     * @param email The user's email.
     */
    void forgotPassword(String email);
    
    /**
     * Verifies the OTP sent for password reset.
     * @param email The user's email.
     * @param otp The One-Time Password.
     */
    void verifyOtp(String email, String otp);
    
    /**
     * Resets the user's password.
     * @param email The user's email.
     * @param otp The verified OTP.
     * @param newPassword The new password.
     */
    void resetPassword(String email, String otp, String newPassword);
}
