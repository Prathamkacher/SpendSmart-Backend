package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.dto.admin.PlatformAnalytics;
import com.spendsmart.auth.dto.admin.TopUserDTO;
import com.spendsmart.auth.dto.admin.TransactionDTO;

import java.util.List;

/**
 * Service interface for administrative operations.
 * Defines methods for managing users, viewing platform-wide data, and system maintenance.
 */
public interface AdminService {
    /**
     * Retrieves all registered users.
     * @return List of user profiles.
     */
    List<UserProfileResponse> getAllUsers();

    /**
     * Suspends a user account.
     * @param userId The ID of the user.
     */
    void suspendUser(Long userId);

    /**
     * Activates a suspended user account.
     * @param userId The ID of the user.
     */
    void activateUser(Long userId);

    /**
     * Deletes a user account.
     * @param userId The ID of the user.
     */
    void deleteUser(Long userId);

    /**
     * Aggregates transactions from across the platform.
     * @return List of transaction details.
     */
    List<TransactionDTO> getAllTransactions();

    /**
     * Provides high-level platform usage analytics.
     * @return Platform analytics summary.
     */
    PlatformAnalytics getPlatformAnalytics();

    /**
     * Identifies top spending users.
     * @return List of top users.
     */
    List<TopUserDTO> getTopSpendingUsers();

    /**
     * Broadcasts a notification to all users.
     * @param title Notification title.
     * @param message Notification body.
     * @param severity Level of severity.
     */
    void sendGlobalNotification(String title, String message, String severity);

    /**
     * Generates a platform report.
     * @return Report data as byte array.
     */
    byte[] exportPlatformReport();

    /**
     * Updates a user's role.
     * @param userId The ID of the user.
     * @param role The new role name.
     */
    void updateUserRole(Long userId, String role);
}
