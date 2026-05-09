// com/spendsmart/auth/dto/UpdateProfileRequest.java
package com.spendsmart.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z ]*$", message = "Name must contain only alphabets and spaces")
    private String fullName;

    @Size(max = 5000000, message = "Avatar data too large (max 5MB)")
    private String avatarUrl;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g., USD, INR)")
    private String currency;

    @Size(max = 250, message = "Bio must be max 250 characters")
    private String bio;

    @Size(max = 50, message = "Timezone must be max 50 characters")
    private String timezone;

    @DecimalMin(value = "0.0", message = "Monthly budget cannot be negative")
    private BigDecimal monthlyBudget;
}