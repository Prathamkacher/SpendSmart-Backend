package com.spendsmart.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private Long userId;
    private String planName; // MONTHLY, YEARLY
}
