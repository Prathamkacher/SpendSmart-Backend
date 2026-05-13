package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.config.JwtService;
import com.spendsmart.auth.constants.AppConstants;
import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.RefreshToken;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.exception.AuthException;
import com.spendsmart.auth.mapper.UserMapper;
import com.spendsmart.auth.repository.RefreshTokenRepository;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Comprehensive Unit Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserMapper userMapper;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private final Long userId = 1L;
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(userId)
                .email(email)
                .fullName("Test User")
                .passwordHash("encodedPassword")
                .isActive(true)
                .role(User.Role.USER)
                .planType(User.PlanType.FREE)
                .currency("INR")
                .timezone("Asia/Kolkata")
                .provider(User.AuthProvider.LOCAL)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setFullName("Test User");
        registerRequest.setPassword("password123");

        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);
        ReflectionTestUtils.setField(authService, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(authService, "adminEmail", "admin@spendsmart.com");
    }

    // --- Register Tests ---

    @Test
    void register_ShouldCreateUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("token");
        when(userMapper.toProfileResponse(any())).thenReturn(new UserProfileResponse());

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        verify(userRepository).save(any());
        verify(emailService).sendWelcomeEmail(any(), any());
        verify(rabbitTemplate, times(2)).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    @Test
    void register_EmailExists_ShouldThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(AuthException.class);
    }

    // --- Login Tests ---

    @Test
    void login_ShouldReturnAuthResponse() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("token");
        when(userMapper.toProfileResponse(any())).thenReturn(new UserProfileResponse());

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("token");
    }

    @Test
    void login_InactiveAccount_ShouldThrowException() {
        testUser.setIsActive(false);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.ACCOUNT_DEACTIVATED);
    }

    @Test
    void login_WrongPassword_ShouldThrowException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("wrong");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.class);
    }

    // --- Logout Tests ---

    @Test
    void logout_ShouldWork() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        authService.logout(userId);
        verify(refreshTokenRepository).revokeAllUserTokens(testUser);
    }

    // --- Token Refresh Tests ---

    @Test
    void refreshToken_ShouldRotate() {
        RefreshToken token = RefreshToken.builder().token("valid").isRevoked(false).expiryDate(LocalDateTime.now().plusDays(1)).user(testUser).build();
        when(refreshTokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("newToken");
        when(userMapper.toProfileResponse(any())).thenReturn(new UserProfileResponse());

        AuthResponse response = authService.refreshToken("valid");

        assertThat(response.getAccessToken()).isEqualTo("newToken");
        verify(refreshTokenRepository).save(token);
        assertThat(token.getIsRevoked()).isTrue();
    }

    @Test
    void refreshToken_Invalid_ShouldThrow() {
        when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.refreshToken("invalid"))
                .isInstanceOf(AuthException.class);
    }

    // --- Profile Tests ---

    @Test
    void getUserById_ShouldReturnProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toProfileResponse(testUser)).thenReturn(new UserProfileResponse());
        
        UserProfileResponse response = authService.getUserById(userId);
        assertThat(response).isNotNull();
    }

    @Test
    void getUserByEmail_ShouldReturnProfile() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toProfileResponse(testUser)).thenReturn(new UserProfileResponse());
        
        UserProfileResponse response = authService.getUserByEmail(email);
        assertThat(response).isNotNull();
    }

    @Test
    void updateProfile_ShouldWork() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setBio("New Bio");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);
        when(userMapper.toProfileResponse(any())).thenReturn(new UserProfileResponse());

        authService.updateProfile(userId, request);
        
        verify(userRepository).save(testUser);
        assertThat(testUser.getFullName()).isEqualTo("Updated Name");
        assertThat(testUser.getBio()).isEqualTo("New Bio");
    }

    @Test
    void changePassword_ShouldWork() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("current");
        request.setNewPassword("new");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("current", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("newEncoded");

        authService.changePassword(userId, request);
        
        verify(userRepository).save(testUser);
        assertThat(testUser.getPasswordHash()).isEqualTo("newEncoded");
        verify(refreshTokenRepository).revokeAllUserTokens(testUser);
    }

    @Test
    void changePassword_WrongCurrent_ShouldThrow() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(userId, request))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void deactivateAccount_ShouldWork() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        authService.deactivateAccount(userId);
        assertThat(testUser.getIsActive()).isFalse();
        verify(refreshTokenRepository).revokeAllUserTokens(testUser);
    }

    @Test
    void getAllUserIds_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        List<Long> ids = authService.getAllUserIds();
        assertThat(ids).hasSize(1).contains(userId);
    }

    // --- OTP & Password Reset Tests ---

    @Test
    void forgotPassword_ShouldGenerateOtp() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        authService.forgotPassword(email);
        
        assertThat(testUser.getResetOtp()).isNotNull();
        assertThat(testUser.getResetOtpExpiry()).isAfter(LocalDateTime.now());
        verify(userRepository).save(testUser);
        verify(emailService).sendOtpEmail(eq(email), anyString());
    }

    @Test
    void verifyOtp_Correct_ShouldSucceed() {
        testUser.setResetOtp("123456");
        testUser.setResetOtpExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        assertDoesNotThrow(() -> authService.verifyOtp(email, "123456"));
    }

    @Test
    void verifyOtp_Wrong_ShouldThrow() {
        testUser.setResetOtp("123456");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        assertThatThrownBy(() -> authService.verifyOtp(email, "wrong"))
                .isInstanceOf(AuthException.class).hasMessage("Invalid OTP.");
    }

    @Test
    void resetPassword_ShouldWork() {
        testUser.setResetOtp("123456");
        testUser.setResetOtpExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("new")).thenReturn("newEncoded");

        authService.resetPassword(email, "123456", "new");
        
        assertThat(testUser.getPasswordHash()).isEqualTo("newEncoded");
        assertThat(testUser.getResetOtp()).isNull();
        verify(refreshTokenRepository).revokeAllUserTokens(testUser);
    }

    // --- Plan Management Tests ---

    @Test
    void upgradeUserPlan_Trial_ShouldWork() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        authService.upgradeUserPlan(userId, "TRIAL", null);
        
        assertThat(testUser.getPlanType()).isEqualTo(User.PlanType.TRIAL);
        assertThat(testUser.getIsTrialUsed()).isTrue();
        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    @Test
    void upgradeUserPlan_Pro_ShouldWork() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        authService.upgradeUserPlan(userId, "PRO", 12);
        
        assertThat(testUser.getPlanType()).isEqualTo(User.PlanType.PRO);
        verify(emailService).sendPremiumActivationEmail(eq(email), anyString(), eq("PRO Yearly"), anyDouble());
    }

    @Test
    void promoteToAdmin_ShouldWork() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        authService.promoteToAdmin(email);
        assertThat(testUser.getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void updateCurrency_ShouldWork() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        authService.updateCurrency(userId, "USD");
        assertThat(testUser.getCurrency()).isEqualTo("USD");
    }

    @Test
    void checkAndDowngradePlan_ShouldDowngradeExpired() {
        testUser.setPlanType(User.PlanType.PRO);
        testUser.setPlanExpiryDate(LocalDateTime.now().minusDays(1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(userMapper.toProfileResponse(testUser)).thenReturn(new UserProfileResponse());

        authService.getUserById(userId);

        assertThat(testUser.getPlanType()).isEqualTo(User.PlanType.FREE);
        assertThat(testUser.getPlanExpiryDate()).isNull();
    }
    @Test
    @DisplayName("forgotPassword() - should reject social provider users")
    void forgotPassword_SocialProvider_ShouldThrow() {
        testUser.setProvider(User.AuthProvider.GOOGLE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        assertThatThrownBy(() -> authService.forgotPassword(email))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Please sign in using GOOGLE");
    }

    @Test
    @DisplayName("forgotPassword() - should reject inactive users")
    void forgotPassword_InactiveUser_ShouldThrow() {
        testUser.setIsActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        assertThatThrownBy(() -> authService.forgotPassword(email))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.ACCOUNT_DEACTIVATED);
    }

    @Test
    @DisplayName("verifyOtp() - should reject expired OTP")
    void verifyOtp_Expired_ShouldThrow() {
        testUser.setResetOtp("123456");
        testUser.setResetOtpExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        assertThatThrownBy(() -> authService.verifyOtp(email, "123456"))
                .isInstanceOf(AuthException.class)
                .hasMessage("OTP has expired.");
    }

    @Test
    @DisplayName("publishAuthEvent() - should handle RabbitMQ failures")
    void publishAuthEvent_Failure_ShouldNotThrow() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("token");
        when(userMapper.toProfileResponse(any())).thenReturn(new UserProfileResponse());
        doThrow(new RuntimeException("MQ down")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("publishSubscriptionEvent() - should handle RabbitMQ failures")
    void publishSubscriptionEvent_Failure_ShouldNotThrow() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doThrow(new RuntimeException("MQ down")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> authService.upgradeUserPlan(userId, "TRIAL", null));
    }
}
