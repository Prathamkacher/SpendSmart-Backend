package com.spendsmart.auth;

import com.spendsmart.auth.dto.LoginRequest;
import com.spendsmart.auth.dto.ProfileUpdateRequest;
import com.spendsmart.auth.dto.RefreshTokenRequest;
import com.spendsmart.auth.dto.RegisterRequest;
import com.spendsmart.auth.dto.ResetPasswordRequest;
import com.spendsmart.auth.dto.UpdateProfileRequest;
import com.spendsmart.auth.dto.VerifyOtpRequest;
import com.spendsmart.auth.dto.admin.TopUserDTO;
import com.spendsmart.auth.dto.admin.TransactionDTO;
import com.spendsmart.shared.events.AuthEvent;
import com.spendsmart.shared.events.NotificationEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SupportDtosTest {

    @Test
    void resetPasswordAndOtpRequests_ShouldRetainAssignedValues() {
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setEmail("user@example.com");
        resetPasswordRequest.setOtp("123456");
        resetPasswordRequest.setNewPassword("Password@123");

        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
        verifyOtpRequest.setEmail("user@example.com");
        verifyOtpRequest.setOtp("123456");

        assertThat(resetPasswordRequest.getNewPassword()).isEqualTo("Password@123");
        assertThat(verifyOtpRequest.getOtp()).isEqualTo("123456");
    }

    @Test
    void loginAndRegisterBuilders_ShouldPopulateFields() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("login@example.com")
                .password("Password@123")
                .build();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Chris Doe")
                .email("register@example.com")
                .password("Password@123")
                .currency("USD")
                .timezone("UTC")
                .build();
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-123");

        assertThat(loginRequest.getEmail()).isEqualTo("login@example.com");
        assertThat(registerRequest.getCurrency()).isEqualTo("USD");
        assertThat(refreshTokenRequest.getRefreshToken()).isEqualTo("refresh-123");
    }

    @Test
    void builderBackedDtos_ShouldExposeConfiguredFields() {
        ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                .fullName("Taylor")
                .currency("USD")
                .bio("Bio")
                .avatarUrl("avatar")
                .build();
        TopUserDTO topUserDTO = TopUserDTO.builder()
                .userId(7L)
                .fullName("Taylor")
                .email("taylor@example.com")
                .totalSpent(new BigDecimal("999.99"))
                .transactionCount(12L)
                .build();
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id("txn-1")
                .userId(7L)
                .userEmail("taylor@example.com")
                .type("EXPENSE")
                .amount(new BigDecimal("10.00"))
                .category("Food")
                .description("Lunch")
                .date(LocalDateTime.now())
                .build();

        assertThat(profileUpdateRequest.getCurrency()).isEqualTo("USD");
        assertThat(topUserDTO.getTransactionCount()).isEqualTo(12L);
        assertThat(transactionDTO.getType()).isEqualTo("EXPENSE");
    }

    @Test
    void eventDtos_ShouldExposeBuilderValuesAndDefaults() {
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .recipientId(9L)
                .type("SYSTEM")
                .severity("INFO")
                .title("Hello")
                .message("World")
                .relatedId(5L)
                .relatedType("USER")
                .build();
        AuthEvent authEvent = AuthEvent.builder()
                .eventType(AuthEvent.EventType.USER_REGISTERED)
                .userId(9L)
                .email("user@example.com")
                .fullName("Jordan")
                .build();

        assertThat(notificationEvent.getRelatedType()).isEqualTo("USER");
        assertThat(authEvent.getEventType()).isEqualTo(AuthEvent.EventType.USER_REGISTERED);
        assertThat(authEvent.getOccurredAt()).isNotNull();
    }

    @Test
    void setterAndConstructorBackedDtos_ShouldExposeValues() {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setRecipientId(3L);
        notificationEvent.setType("BUDGET_ALERT");
        notificationEvent.setSeverity("WARNING");
        notificationEvent.setTitle("Budget warning");
        notificationEvent.setMessage("Threshold reached");
        notificationEvent.setRelatedId(88L);
        notificationEvent.setRelatedType("BUDGET");

        TopUserDTO topUserDTO = new TopUserDTO(1L, "Jamie", "jamie@example.com", new BigDecimal("250.00"), 4L);
        TransactionDTO transactionDTO = new TransactionDTO("tx-2", 1L, "jamie@example.com", "INCOME", new BigDecimal("22.00"), "Salary", "April", LocalDateTime.now());
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .fullName("Jamie Updated")
                .bio("Updated bio")
                .currency("EUR")
                .timezone("Europe/Berlin")
                .build();

        assertThat(notificationEvent.getTitle()).isEqualTo("Budget warning");
        assertThat(topUserDTO.getEmail()).isEqualTo("jamie@example.com");
        assertThat(transactionDTO.getCategory()).isEqualTo("Salary");
        assertThat(updateProfileRequest.getTimezone()).isEqualTo("Europe/Berlin");
    }
}
