package com.spendsmart.payment.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaymentDtoTest {

    @Test
    void testOrderResponse() {
        OrderResponse response = OrderResponse.builder()
                .id("order_123")
                .amount(new BigDecimal("1000"))
                .currency("INR")
                .keyId("rzp_test_key")
                .build();

        assertEquals("order_123", response.getId());
        assertEquals(new BigDecimal("1000"), response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals("rzp_test_key", response.getKeyId());
        assertNotNull(response.toString());
    }

    @Test
    void testCreateOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setPlanName("MONTHLY");

        assertEquals(1L, request.getUserId());
        assertEquals("MONTHLY", request.getPlanName());
        assertNotNull(request.toString());
    }

    @Test
    void testVerifyPaymentRequest() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_1");
        request.setRazorpayPaymentId("pay_1");
        request.setRazorpaySignature("sig_1");

        assertEquals("order_1", request.getRazorpayOrderId());
        assertNotNull(request.toString());
    }
}
