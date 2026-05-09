package com.spendsmart.payment.controller;

import com.razorpay.RazorpayException;
import com.spendsmart.payment.dto.CreateOrderRequest;
import com.spendsmart.payment.dto.OrderResponse;
import com.spendsmart.payment.dto.VerifyPaymentRequest;
import com.spendsmart.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Unit Tests")
class PaymentControllerTest {

    @Mock private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    @DisplayName("createOrder() - should return OK")
    void createOrder_ShouldReturnOk() throws RazorpayException {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setUserId(1L);
        req.setPlanName("MONTHLY");

        OrderResponse orderResponse = OrderResponse.builder().id("order_123").amount(new BigDecimal("199")).build();
        when(paymentService.createOrder(any())).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = paymentController.createOrder(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo("order_123");
    }

    @Test
    @DisplayName("verifyPayment() - should return true")
    void verifyPayment_ShouldReturnTrue() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        when(paymentService.verifyPayment(any())).thenReturn(true);

        ResponseEntity<Boolean> response = paymentController.verifyPayment(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    @DisplayName("verifyPayment() - should return false on invalid")
    void verifyPayment_Invalid_ShouldReturnFalse() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        when(paymentService.verifyPayment(any())).thenReturn(false);

        ResponseEntity<Boolean> response = paymentController.verifyPayment(req);

        assertThat(response.getBody()).isFalse();
    }
}
