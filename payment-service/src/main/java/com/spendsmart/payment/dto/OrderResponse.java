package com.spendsmart.payment.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderResponse {
    private String id; // Razorpay Order ID
    private BigDecimal amount;
    private String currency;
    private String keyId; // Razorpay Key ID for frontend
}
