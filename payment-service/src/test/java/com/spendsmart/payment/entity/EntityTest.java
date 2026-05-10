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
        payment.setRazorpaySignature("sig_1");
        payment.setAmount(new BigDecimal("100"));
        payment.setCurrency("INR");
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPlanName("PREMIUM");
        payment.setCreatedAt(now);

        assertEquals(1L, payment.getId());
        assertEquals(2L, payment.getUserId());
        assertEquals("order_1", payment.getRazorpayOrderId());
        assertEquals("rzp_1", payment.getRazorpayPaymentId());
        assertEquals("sig_1", payment.getRazorpaySignature());
        assertEquals(new BigDecimal("100"), payment.getAmount());
        assertEquals("INR", payment.getCurrency());
        assertEquals(Payment.PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("PREMIUM", payment.getPlanName());
        assertEquals(now, payment.getCreatedAt());

        Payment payment2 = new Payment(1L, 2L, new BigDecimal("100"), "INR", Payment.PaymentStatus.SUCCESS, "order_1", "rzp_1", "sig_1", "PREMIUM", now);
        assertEquals(payment, payment2);
        assertEquals(payment.hashCode(), payment2.hashCode());
        assertNotNull(payment.toString());
        
        Payment payment3 = Payment.builder()
            .id(1L)
            .userId(2L)
            .amount(new BigDecimal("100"))
            .currency("INR")
            .status(Payment.PaymentStatus.SUCCESS)
            .razorpayOrderId("order_1")
            .razorpayPaymentId("rzp_1")
            .razorpaySignature("sig_1")
            .planName("PREMIUM")
            .createdAt(now)
            .build();
            
        assertEquals(payment, payment3);
        assertNotNull(Payment.builder().toString());
    }
    
    @Test
    void testEnum() {
        Payment.PaymentStatus[] values = Payment.PaymentStatus.values();
        assertTrue(values.length > 0);
        
        Payment.PaymentStatus status = Payment.PaymentStatus.valueOf("SUCCESS");
        assertEquals(Payment.PaymentStatus.SUCCESS, status);
    }
}
