package com.spendsmart.payment.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testPayment() {
        Payment payment = new Payment();
        LocalDateTime now = LocalDateTime.now();

        payment.setId(1L);
        payment.setUserId(2L);
        payment.setRazorpayOrderId("order_1");
        payment.setRazorpayPaymentId("rzp_1");
        payment.setAmount(new BigDecimal("100"));
        payment.setCurrency("INR");
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPlanName("PREMIUM");
        payment.setCreatedAt(now);

        assertEquals(1L, payment.getId());
        assertEquals("order_1", payment.getRazorpayOrderId());
        assertEquals(Payment.PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(now, payment.getCreatedAt());

        assertNotNull(payment.toString());
    }
}
