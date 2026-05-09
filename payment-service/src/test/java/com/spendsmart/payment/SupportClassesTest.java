package com.spendsmart.payment;

import com.spendsmart.payment.dto.CreateOrderRequest;
import com.spendsmart.payment.dto.OrderResponse;
import com.spendsmart.payment.dto.VerifyPaymentRequest;
import com.spendsmart.payment.entity.Payment;
import com.spendsmart.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SupportClassesTest {

    @Test
    void apiResponseFactories_ShouldPopulateExpectedFields() {
        ApiResponse<String> success = ApiResponse.success("created", "order_123");
        ApiResponse<Void> error = ApiResponse.error("failed");

        assertThat(success.isSuccess()).isTrue();
        assertThat(success.getData()).isEqualTo("order_123");
        assertThat(success.getTimestamp()).isNotNull();
        assertThat(error.isSuccess()).isFalse();
        assertThat(error.getMessage()).isEqualTo("failed");
    }

    @Test
    void requestAndResponseDtos_ShouldRoundTripData() {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(15L);
        createOrderRequest.setPlanName("MONTHLY");

        VerifyPaymentRequest verifyPaymentRequest = new VerifyPaymentRequest();
        verifyPaymentRequest.setUserId(15L);
        verifyPaymentRequest.setRazorpayOrderId("order_1");
        verifyPaymentRequest.setRazorpayPaymentId("pay_1");
        verifyPaymentRequest.setRazorpaySignature("sig_1");

        OrderResponse response = OrderResponse.builder()
                .id("order_1")
                .amount(new BigDecimal("199"))
                .currency("INR")
                .keyId("key")
                .build();

        assertThat(createOrderRequest.getUserId()).isEqualTo(15L);
        assertThat(createOrderRequest.getPlanName()).isEqualTo("MONTHLY");
        assertThat(verifyPaymentRequest.getRazorpayPaymentId()).isEqualTo("pay_1");
        assertThat(response.getAmount()).isEqualByComparingTo("199");
        assertThat(response.getCurrency()).isEqualTo("INR");
    }

    @Test
    void paymentBuilder_ShouldPopulateEntityFields() {
        Payment payment = Payment.builder()
                .id(1L)
                .userId(5L)
                .amount(new BigDecimal("1499"))
                .currency("INR")
                .status(Payment.PaymentStatus.SUCCESS)
                .razorpayOrderId("order_123")
                .razorpayPaymentId("pay_123")
                .razorpaySignature("sig")
                .planName("YEARLY")
                .build();

        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.SUCCESS);
        assertThat(payment.getPlanName()).isEqualTo("YEARLY");
        assertThat(payment.getRazorpayOrderId()).isEqualTo("order_123");
    }
}
