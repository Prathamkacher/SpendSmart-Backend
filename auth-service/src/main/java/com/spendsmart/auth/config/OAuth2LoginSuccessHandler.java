package com.spendsmart.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.entity.RefreshToken;
import com.spendsmart.auth.mapper.UserMapper;
import com.spendsmart.auth.repository.RefreshTokenRepository;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler for successful OAuth2 authentication.
 * Manages user creation/lookup, JWT token generation, and redirection to the frontend.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException {

        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

            String email;
            String name;
            String picture;
            User.AuthProvider provider;

            if ("google".equalsIgnoreCase(clientRegistrationId)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                picture = (String) attributes.get("picture");
                provider = User.AuthProvider.GOOGLE;
            } else if ("github".equalsIgnoreCase(clientRegistrationId)) {
                // GitHub might not return email if it's private, but we need it as a unique key
                email = (String) attributes.get("email");
                if (email == null) {
                    // Fallback to login name if email is missing (not ideal, but common for GitHub)
                    email = attributes.get("login") + "@github.com";
                }
                name = (String) attributes.get("name");
                if (name == null) name = (String) attributes.get("login");
                picture = (String) attributes.get("avatar_url");
                provider = User.AuthProvider.GITHUB;
            } else {
                log.error("Unknown OAuth2 provider: {}", clientRegistrationId);
                response.sendRedirect(frontendUrl + "/auth/login?error=unknown_provider");
                return;
            }

            log.info("Processing OAuth2 login for: {}", email);

            final String finalEmail = email;
            final String finalName = name;
            final String finalPicture = picture;
            final User.AuthProvider finalProvider = provider;

            // ✅ Find or create user
            boolean isNewUser = !userRepository.existsByEmail(finalEmail.toLowerCase());
            User user = userRepository.findByEmail(finalEmail.toLowerCase()).orElseGet(() -> {
                log.info("Creating new OAuth2 user: {}", finalEmail);
                User newUser = User.builder()
                        .fullName(finalName != null ? finalName : finalEmail)
                        .email(finalEmail.toLowerCase())
                        .passwordHash(null)
                        .provider(finalProvider)
                        .avatarUrl(finalPicture)
                        .isActive(true)
                        .role(User.Role.USER)
                        .build();
                return userRepository.saveAndFlush(newUser);
            });

            if (isNewUser) {
                emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
            }

            // ❌ If disabled
            if (!user.getIsActive()) {
                log.warn("Login attempt for deactivated user: {}", email);
                response.sendRedirect(frontendUrl + "/auth/login?error=account_deactivated");
                return;
            }

            // ✅ Access token
            String accessToken = jwtService.generateToken(
                    user.getEmail(),
                    user.getUserId(),
                    user.getRole().name(),
                    user.getPlanType().name()
            );

            // ✅ Refresh token
            String refreshTokenValue = UUID.randomUUID().toString();

            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(refreshTokenValue)
                    .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                    .isRevoked(false)
                    .build();

            refreshTokenRepository.saveAndFlush(refreshToken);

            // ✅ User profile
            UserProfileResponse userProfile = userMapper.toProfileResponse(user);
            
            // 🔥 CRITICAL FIX: If the user has a massive Base64 local image, it will make the session
            // string too large for Tomcat's HTTP header limits (resulting in HTTP 500).
            // We strip it from the URL redirect payload; the Angular frontend fetches the full profile anyway.
            userProfile.setAvatarUrl(null);

            // ✅ Session object
            Map<String, Object> sessionData = new LinkedHashMap<>();
            sessionData.put("accessToken", accessToken);
            sessionData.put("refreshToken", refreshTokenValue);
            sessionData.put("tokenType", "Bearer");
            sessionData.put("expiresIn", jwtExpiration);
            sessionData.put("user", userProfile);
            sessionData.put("message", "Welcome back, " + user.getFullName() + "!");

            // ✅ Encode session
            String sessionJson = objectMapper.writeValueAsString(sessionData);
            String sessionBase64 = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(sessionJson.getBytes(StandardCharsets.UTF_8));

            log.info("OAuth2 login successful for: {}. Redirecting to frontend.", email);

            // 🔥 IMPORTANT: Redirect with session as a URL parameter to the dedicated callback route
            response.sendRedirect(frontendUrl + "/auth/callback?session=" + sessionBase64);

        } catch (Exception e) {
            log.error("CRITICAL ERROR during OAuth2 success handling: ", e);
            response.sendRedirect(frontendUrl + "/auth/login?error=oauth_failed");
        }
    }
}
