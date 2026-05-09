// com/spendsmart/auth/dto/AuthResponse.java
package com.spendsmart.auth.dto;

import lombok.*;

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