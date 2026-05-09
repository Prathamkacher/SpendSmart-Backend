package com.spendsmart.auth.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopUserDTO {
    private Long userId;
    private String fullName;
    private String email;
    private BigDecimal totalSpent;
    private long transactionCount;
}
