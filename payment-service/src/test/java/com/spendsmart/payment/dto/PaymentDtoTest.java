package com.spendsmart.payment.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaymentDtoTest {

    @Test
    void testOrderResponse() {
        OrderResponse response = buildOrderResponse();
        OrderResponse response2 = buildOrderResponse();
        OrderResponse response3 = OrderResponse.builder()
                .id("order_456")
                .amount(new BigDecimal("2000"))
                .currency("USD")
                .keyId("rzp_other_key")
                .build();

        assertEquals("order_123", response.getId());
        assertEquals(new BigDecimal("1000"), response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals("rzp_test_key", response.getKeyId());
                
        assertEquals(response, response2);
        assertEquals(response.hashCode(), response2.hashCode());
        assertNotEquals(response, response3);
        
        assertNotNull(response.toString());
        assertNotNull(OrderResponse.builder().toString());
    }

    @Test
    void testCreateOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setPlanName("MONTHLY");

        assertEquals(1L, request.getUserId());
        assertEquals("MONTHLY", request.getPlanName());
        
        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setUserId(1L);
        request2.setPlanName("MONTHLY");
        
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        CreateOrderRequest request3 = new CreateOrderRequest();
        request3.setUserId(2L);
        assertNotEquals(request, request3);
        assertNotNull(request.toString());
    }

    @Test
    void testVerifyPaymentRequest() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setUserId(1L);
        request.setRazorpayOrderId("order_1");
        request.setRazorpayPaymentId("pay_1");
        request.setRazorpaySignature("sig_1");

        assertEquals("order_1", request.getRazorpayOrderId());
        assertEquals("pay_1", request.getRazorpayPaymentId());
        assertEquals("sig_1", request.getRazorpaySignature());
        assertEquals(1L, request.getUserId());

        VerifyPaymentRequest request2 = new VerifyPaymentRequest();
        request2.setUserId(1L);
        request2.setRazorpayOrderId("order_1");
        request2.setRazorpayPaymentId("pay_1");
        request2.setRazorpaySignature("sig_1");
        
        assertEquals(request, request2);
        assertEquals(request.hashCode(), request2.hashCode());
        
        VerifyPaymentRequest request3 = new VerifyPaymentRequest();
        request3.setUserId(2L);
        assertNotEquals(request, request3);
        assertNotNull(request.toString());
    }

    @Test
    void testNotificationRequest() {
        NotificationRequest request = NotificationRequest.builder()
                .recipientId(5L)
                .type("PAYMENT_RECEIPT")
                .severity("INFO")
                .title("Payment complete")
                .message("Premium activated")
                .relatedId(88L)
                .relatedType("PAYMENT")
                .build();
        NotificationRequest same = new NotificationRequest(
                5L,
                "PAYMENT_RECEIPT",
                "INFO",
                "Payment complete",
                "Premium activated",
                88L,
                "PAYMENT"
        );

        assertEquals(5L, request.getRecipientId());
        assertEquals("PAYMENT_RECEIPT", request.getType());
        assertEquals("INFO", request.getSeverity());
        assertEquals("Payment complete", request.getTitle());
        assertEquals("Premium activated", request.getMessage());
        assertEquals(88L, request.getRelatedId());
        assertEquals("PAYMENT", request.getRelatedType());
        assertEquals(request, same);
        assertEquals(request.hashCode(), same.hashCode());
        assertNotNull(request.toString());
    }

    private OrderResponse buildOrderResponse() {
        return OrderResponse.builder()
                .id("order_123")
                .amount(new BigDecimal("1000"))
                .currency("INR")
                .keyId("rzp_test_key")
                .build();
    }
}
