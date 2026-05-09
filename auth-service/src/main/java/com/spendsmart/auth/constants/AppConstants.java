// com/spendsmart/auth/constants/AppConstants.java
package com.spendsmart.auth.constants;

/**
 * Central place for all string/numeric constants.
 * SonarQube rule: no magic literals scattered in code.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class — prevent instantiation
    }

    // --- Exception messages ---
    public static final String USER_NOT_FOUND         = "User not found with the given identifier";
    public static final String EMAIL_ALREADY_EXISTS   = "An account with this email already exists";
    public static final String INVALID_CREDENTIALS    = "Invalid email or password";
    public static final String TOKEN_INVALID          = "Token is invalid or malformed";
    public static final String TOKEN_EXPIRED          = "Token has expired";
    public static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token not found";
    public static final String REFRESH_TOKEN_EXPIRED  = "Refresh token has expired. Please login again";
    public static final String ACCOUNT_DEACTIVATED    = "This account has been deactivated";
    public static final String UNAUTHORIZED           = "You are not authorized to perform this action";
    public static final String CURRENT_PASSWORD_WRONG = "Current password is incorrect";

    // --- Success messages ---
    public static final String REGISTER_SUCCESS       = "Account created successfully";
    public static final String LOGIN_SUCCESS          = "Login successful";
    public static final String LOGOUT_SUCCESS         = "Logged out successfully";
    public static final String PROFILE_UPDATE_SUCCESS = "Profile updated successfully";
    public static final String PASSWORD_CHANGE_SUCCESS = "Password changed successfully";
    public static final String CURRENCY_UPDATE_SUCCESS = "Currency preference updated";
    public static final String ACCOUNT_DEACTIVATE_SUCCESS = "Account deactivated successfully";
    public static final String TOKEN_REFRESH_SUCCESS  = "Token refreshed successfully";

    // --- RabbitMQ ---
    public static final String AUTH_EXCHANGE        = "auth.exchange";
    public static final String AUTH_ROUTING_KEY     = "auth.event.key";
    public static final String AUTH_QUEUE           = "auth.queue";

    // --- Notification Service ---
    public static final String NOTIFICATION_EXCHANGE    = "notification.exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routingKey";

    // --- JWT ---
    public static final String BEARER_PREFIX           = "Bearer ";
    public static final String AUTH_HEADER             = "Authorization";

    // --- Roles ---
    public static final String ROLE_USER               = "ROLE_USER";
    public static final String ROLE_ADMIN              = "ROLE_ADMIN";

    // --- Auth providers ---
    public static final String PROVIDER_LOCAL          = "LOCAL";
    public static final String PROVIDER_GOOGLE         = "GOOGLE";

    // --- Default values ---
    public static final String DEFAULT_CURRENCY        = "INR";
    public static final String DEFAULT_TIMEZONE        = "Asia/Kolkata";

    // --- Swagger tags ---
    public static final String SWAGGER_TAG_AUTH        = "Authentication";
    public static final String SWAGGER_TAG_PROFILE     = "User Profile";
}