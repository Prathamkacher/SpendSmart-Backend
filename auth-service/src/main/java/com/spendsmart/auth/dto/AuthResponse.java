// com/spendsmart/auth/dto/AuthResponse.java
package com.spendsmart.auth.dto;

import lombok.*;

/**
 * Data Transfer Object for authentication responses.
 * Contains access and refresh tokens, token metadata, and user profile details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;       // always "Bearer"
    private Long   expiresIn;       // milliseconds
    private UserProfileResponse user;
}