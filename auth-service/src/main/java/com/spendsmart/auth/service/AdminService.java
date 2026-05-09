package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.dto.admin.PlatformAnalytics;
import com.spendsmart.auth.dto.admin.TopUserDTO;
import com.spendsmart.auth.dto.admin.TransactionDTO;

import java.util.List;

public interface AdminService {
    List<UserProfileResponse> getAllUsers();
    void suspendUser(Long userId);
    void activateUser(Long userId);
    void deleteUser(Long userId);
    List<TransactionDTO> getAllTransactions();
    PlatformAnalytics getPlatformAnalytics();
    List<TopUserDTO> getTopSpendingUsers();
    void sendGlobalNotification(String title, String message, String severity);
    byte[] exportPlatformReport();
}
