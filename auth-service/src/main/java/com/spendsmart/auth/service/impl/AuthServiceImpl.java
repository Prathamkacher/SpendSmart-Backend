package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.config.JwtService;
import com.spendsmart.auth.constants.AppConstants;
import com.spendsmart.auth.dto.AuthResponse;
import com.spendsmart.auth.dto.ChangePasswordRequest;
import com.spendsmart.auth.dto.ForgotPasswordRequest;
import com.spendsmart.auth.dto.LoginRequest;
import com.spendsmart.auth.dto.ProfileUpdateRequest;
import com.spendsmart.auth.dto.RefreshTokenRequest;
import com.spendsmart.auth.dto.RegisterRequest;
import com.spendsmart.auth.dto.ResetPasswordRequest;
import com.spendsmart.auth.dto.UpdateProfileRequest;
import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.dto.VerifyOtpRequest;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.events.NotificationEvent;
import com.spendsmart.auth.entity.RefreshToken;
import com.spendsmart.auth.entity.User;
import com.spendsmart.shared.events.AuthEvent;
import com.spendsmart.auth.exception.AuthException;
import com.spendsmart.shared.exception.ResourceNotFoundException;
import com.spendsmart.auth.mapper.UserMapper;
import com.spendsmart.auth.repository.RefreshTokenRepository;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.service.AuthService;
import com.spendsmart.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtService             jwtService;
    private final UserMapper             userMapper;
    private final RabbitTemplate         rabbitTemplate;
    private final EmailService           emailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    @Value("${jwt.expiration:3600000}")
    private Long jwtExpiration;
    
    @Value("${spring.mail.username:admin@spendsmart.com}")
    private String adminEmail;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("Registering user: {}", email);
        
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(AppConstants.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .fullName(normalizeFullName(request.getFullName()))
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .currency(request.getCurrency() != null ? request.getCurrency() : AppConstants.DEFAULT_CURRENCY)
                .timezone(request.getTimezone() != null ? request.getTimezone() : AppConstants.DEFAULT_TIMEZONE)
                .provider(User.AuthProvider.LOCAL)
                .isActive(true)
                .role(User.Role.USER)
                .planType(User.PlanType.FREE)
                .build();

        User savedUser = userRepository.save(user);
        
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
        emailService.sendAdminNotificationNewUser(adminEmail, savedUser);
        
        publishAuthEvent(AuthEvent.EventType.USER_REGISTERED, savedUser);
        return buildAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("Login attempt: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AppConstants.INVALID_CREDENTIALS));

        user = checkAndDowngradePlan(user);

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthException(AppConstants.ACCOUNT_DEACTIVATED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException(AppConstants.INVALID_CREDENTIALS);
        }

        publishAuthEvent(AuthEvent.EventType.USER_LOGGED_IN, user);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        User user = findUserById(userId);
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("User {} logged out", userId);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .filter(t -> !t.getIsRevoked() && t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new AuthException(AppConstants.REFRESH_TOKEN_NOT_FOUND));

        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = checkAndDowngradePlan(refreshToken.getUser());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long userId) {
        return userMapper.toProfileResponse(findUserById(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));
        return userMapper.toProfileResponse(checkAndDowngradePlan(user));
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);
        
        if (request.getFullName() != null) user.setFullName(normalizeFullName(request.getFullName()));
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getCurrency() != null) user.setCurrency(request.getCurrency());
        if (request.getTimezone() != null) user.setTimezone(request.getTimezone());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getMonthlyBudget() != null) user.setMonthlyBudget(request.getMonthlyBudget());
        
        return userMapper.toProfileResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthException(AppConstants.CURRENT_PASSWORD_WRONG);
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = findUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
        refreshTokenRepository.revokeAllUserTokens(user);
        publishAuthEvent(AuthEvent.EventType.USER_DEACTIVATED, user);
    }

    @Override
    public List<Long> getAllUserIds() {
        return userRepository.findAll().stream().map(User::getUserId).toList();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getIsActive())) throw new AuthException(AppConstants.ACCOUNT_DEACTIVATED);
        if (user.getProvider() != User.AuthProvider.LOCAL) {
            throw new AuthException("Please sign in using " + user.getProvider());
        }

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Override
    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            throw new AuthException("Invalid OTP.");
        }
        if (user.getResetOtpExpiry() == null || user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new AuthException("OTP has expired.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        verifyOtp(email, otp);
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    @Override
    @Transactional
    public void upgradeUserPlan(Long userId, String planType, Integer durationMonths) {
        User user = findUserById(userId);
        User.PlanType newPlan = User.PlanType.valueOf(planType);
        
        user.setPlanType(newPlan);
        user.setPlanStartDate(LocalDateTime.now());
        
        if (newPlan == User.PlanType.TRIAL) {
            user.setPlanExpiryDate(LocalDateTime.now().plusDays(7));
            user.setIsTrialUsed(true);
            publishSubscriptionEvent(user, "trial.started", "7-day trial started!");
        } else if (newPlan == User.PlanType.PRO) {
            int months = durationMonths != null ? durationMonths : 1;
            user.setPlanExpiryDate(LocalDateTime.now().plusMonths(months));
            double amount = (months == 12) ? 1499.0 : 199.0 * months;
            emailService.sendPremiumActivationEmail(user.getEmail(), user.getFullName(), "PRO", amount);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void promoteToAdmin(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(User.Role.ADMIN);
        userRepository.save(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::checkAndDowngradePlan)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND));
    }

    private User checkAndDowngradePlan(User user) {
        if (user.getPlanType() != User.PlanType.FREE
                && user.getPlanExpiryDate() != null
                && user.getPlanExpiryDate().isBefore(LocalDateTime.now())) {
            log.info("Plan expired for user {}. Downgrading.", user.getUserId());
            user.setPlanType(User.PlanType.FREE);
            user.setPlanExpiryDate(null);
            user.setPlanStartDate(null);
            return userRepository.save(user);
        }
        return user;
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user.getEmail(), user.getUserId(), user.getRole().name(), user.getPlanType().name());
        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user).token(refreshTokenValue).isRevoked(false)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000)).build());
        
        return AuthResponse.builder()
                .accessToken(accessToken).refreshToken(refreshTokenValue)
                .tokenType("Bearer").expiresIn(jwtExpiration)
                .user(userMapper.toProfileResponse(user)).build();
    }

    private void publishAuthEvent(AuthEvent.EventType type, User user) {
        try {
            rabbitTemplate.convertAndSend(AppConstants.AUTH_EXCHANGE, AppConstants.AUTH_ROUTING_KEY, 
                AuthEvent.builder().eventType(type).userId(user.getUserId()).email(user.getEmail()).fullName(user.getFullName()).build());
        } catch (Exception ex) {
            log.error("Auth event error: {}", ex.getMessage());
        }
    }

    private void publishSubscriptionEvent(User user, String type, String message) {
        try {
            rabbitTemplate.convertAndSend(AppConstants.NOTIFICATION_EXCHANGE, AppConstants.NOTIFICATION_ROUTING_KEY,
                NotificationEvent.builder().recipientId(user.getUserId()).type("SYSTEM").severity("INFO").title(type.toUpperCase()).message(message).build());
        } catch (Exception ex) {
            log.error("Subscription event error: {}", ex.getMessage());
        }
    }

    private String normalizeEmail(String email) { return email != null ? email.trim().toLowerCase() : null; }
    private String normalizeFullName(String name) { return name != null ? name.trim().replaceAll("\\s+", " ") : null; }

    @Override
    @Transactional
    public void updateCurrency(Long userId, String currency) {
        User user = findUserById(userId);
        user.setCurrency(currency);
        userRepository.save(user);
    }
}
