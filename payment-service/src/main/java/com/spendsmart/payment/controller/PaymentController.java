package com.spendsmart.payment.controller;

import com.razorpay.RazorpayException;
import com.spendsmart.payment.dto.CreateOrderRequest;
import com.spendsmart.payment.dto.OrderResponse;
import com.spendsmart.payment.dto.VerifyPaymentRequest;
import com.spendsmart.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) throws RazorpayException {
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }
}
