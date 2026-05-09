package com.spendsmart.recurring.service;

import com.spendsmart.recurring.dto.RecurringRequest;
import com.spendsmart.recurring.dto.RecurringResponse;

import java.util.List;

public interface RecurringService {
    RecurringResponse addRecurring(Long userId, RecurringRequest request);
    List<RecurringResponse> getByUser(Long userId);
    RecurringResponse getById(Long recurringId);
    List<RecurringResponse> getActiveRecurring(Long userId);
    RecurringResponse updateRecurring(Long recurringId, Long userId, RecurringRequest request);
    RecurringResponse deactivateRecurring(Long recurringId, Long userId);
    RecurringResponse activateRecurring(Long recurringId, Long userId);
    void deleteRecurring(Long recurringId, Long userId);
    
    // Core Logic
    void processUpcomingDue();
    void generateTransactionFromRecurring(Long recurringId);
}
