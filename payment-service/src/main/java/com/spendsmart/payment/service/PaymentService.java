package com.spendsmart.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.spendsmart.payment.client.AuthClient;
import com.spendsmart.payment.dto.CreateOrderRequest;
import com.spendsmart.payment.dto.OrderResponse;
import com.spendsmart.payment.dto.VerifyPaymentRequest;
import com.spendsmart.payment.entity.Payment;
import com.spendsmart.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;
    private final AuthClient authClient;
    private RazorpayClient razorpay;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpay = new RazorpayClient(keyId, keySecret);
    }

    private static final String CURRENCY_INR = "INR";
    private static final String PLAN_MONTHLY = "MONTHLY";
    private static final String PLAN_YEARLY = "YEARLY";
    private static final String PRO_PLAN = "PRO";

    private static final Map<String, BigDecimal> PLAN_PRICES = Map.of(
            PLAN_MONTHLY, new BigDecimal("199"),
            PLAN_YEARLY, new BigDecimal("1499")
    );

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) throws RazorpayException {
        BigDecimal amount = PLAN_PRICES.get(request.getPlanName());
        if (amount == null) throw new IllegalArgumentException("Invalid plan name");

        // Razorpay expects amount in paise (Rupees * 100)
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount.multiply(new BigDecimal("100")).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = razorpay.orders.create(orderRequest);

        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .amount(amount)
                .currency(CURRENCY_INR)
                .status(Payment.PaymentStatus.PENDING)
                .razorpayOrderId(order.get("id"))
                .planName(request.getPlanName())
                .build();

        paymentRepository.save(payment);

        return OrderResponse.builder()
                .id(order.get("id"))
                .amount(amount)
                .currency("INR")
                .keyId(keyId)
                .build();
    }

    @Transactional
    public boolean verifyPayment(VerifyPaymentRequest request) {
        try {
            // Verify signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                        .orElseThrow(() -> new RuntimeException("Payment record not found"));

                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                paymentRepository.save(payment);

                // Upgrade user plan via Auth Service
                try {
                    int duration = payment.getPlanName().equals(PLAN_YEARLY) ? 12 : 1;
                    authClient.upgradeUserPlan(request.getUserId(), PRO_PLAN, duration);
                    log.info("Payment verified and plan upgraded for user: {}", request.getUserId());
                } catch (Exception e) {
                    log.error("Payment verified but Auth Service plan upgrade failed for user: {}. Error: {}", request.getUserId(), e.getMessage());
                    // In a production environment, we would likely trigger a retry or manual intervention event here.
                    // For now, we still return true because the payment WAS successful.
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Payment verification failed: {}", e.getMessage());
        }
        return false;
    }
}
