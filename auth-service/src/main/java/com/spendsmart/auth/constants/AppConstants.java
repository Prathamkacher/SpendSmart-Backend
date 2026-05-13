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
    /** Error message when user is not found. */
    public static final String USER_NOT_FOUND         = "User not found with the given identifier";
    /** Error message when email already exists. */
    public static final String EMAIL_ALREADY_EXISTS   = "An account with this email already exists";
    /** Error message for invalid login credentials. */
    public static final String INVALID_CREDENTIALS    = "Invalid email or password";
    /** Error message for invalid tokens. */
    public static final String TOKEN_INVALID          = "Token is invalid or malformed";
    /** Error message for expired tokens. */
    public static final String TOKEN_EXPIRED          = "Token has expired";
    /** Error message when refresh token is missing. */
    public static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token not found";
    /** Error message when refresh token is expired. */
    public static final String REFRESH_TOKEN_EXPIRED  = "Refresh token has expired. Please login again";
    /** Error message for deactivated accounts. */
    public static final String ACCOUNT_DEACTIVATED    = "This account has been deactivated";
    /** Error message for unauthorized access. */
    public static final String UNAUTHORIZED           = "You are not authorized to perform this action";
    /** Error message for wrong current password. */
    public static final String CURRENT_PASSWORD_WRONG = "Current password is incorrect";

    // --- Success messages ---
    /** Success message for registration. */
    public static final String REGISTER_SUCCESS       = "Account created successfully";
    /** Success message for login. */
    public static final String LOGIN_SUCCESS          = "Login successful";
    /** Success message for logout. */
    public static final String LOGOUT_SUCCESS         = "Logged out successfully";
    /** Success message for profile updates. */
    public static final String PROFILE_UPDATE_SUCCESS = "Profile updated successfully";
    /** Success message for password changes. */
    public static final String PASSWORD_CHANGE_SUCCESS = "Password changed successfully";
    /** Success message for currency updates. */
    public static final String CURRENCY_UPDATE_SUCCESS = "Currency preference updated";
    /** Success message for account deactivation. */
    public static final String ACCOUNT_DEACTIVATE_SUCCESS = "Account deactivated successfully";
    /** Success message for token refresh. */
    public static final String TOKEN_REFRESH_SUCCESS  = "Token refreshed successfully";

    // --- RabbitMQ ---
    /** Exchange name for authentication events. */
    public static final String AUTH_EXCHANGE        = "auth.exchange";
    /** Routing key for authentication events. */
    public static final String AUTH_ROUTING_KEY     = "auth.event.key";
    /** Queue name for authentication events. */
    public static final String AUTH_QUEUE           = "auth.queue";

    // --- Notification Service ---
    /** Exchange name for notification events. */
    public static final String NOTIFICATION_EXCHANGE    = "notification.exchange";
    /** Routing key for notification events. */
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routingKey";

    // --- JWT ---
    /** Prefix for Bearer tokens. */
    public static final String BEARER_PREFIX           = "Bearer ";
    /** Standard Authorization header name. */
    public static final String AUTH_HEADER             = "Authorization";

    // --- Roles ---
    /** Regular user role. */
    public static final String ROLE_USER               = "ROLE_USER";
    /** Administrator role. */
    public static final String ROLE_ADMIN              = "ROLE_ADMIN";

    // --- Auth providers ---
    /** Local authentication provider. */
    public static final String PROVIDER_LOCAL          = "LOCAL";
    /** Google OAuth2 provider. */
    public static final String PROVIDER_GOOGLE         = "GOOGLE";

    // --- Default values ---
    /** Default currency for new users. */
    public static final String DEFAULT_CURRENCY        = "INR";
    /** Default timezone for new users. */
    public static final String DEFAULT_TIMEZONE        = "Asia/Kolkata";

    // --- Swagger tags ---
    /** Swagger tag for authentication endpoints. */
    public static final String SWAGGER_TAG_AUTH        = "Authentication";
    /** Swagger tag for profile endpoints. */
    public static final String SWAGGER_TAG_PROFILE     = "User Profile";
}