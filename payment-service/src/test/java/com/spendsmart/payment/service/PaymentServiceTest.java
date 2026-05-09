package com.spendsmart.payment.service;

import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.spendsmart.payment.client.AuthClient;
import com.spendsmart.payment.dto.CreateOrderRequest;
import com.spendsmart.payment.dto.OrderResponse;
import com.spendsmart.payment.dto.VerifyPaymentRequest;
import com.spendsmart.payment.entity.Payment;
import com.spendsmart.payment.repository.PaymentRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private AuthClient authClient;
    @Mock private RazorpayClient razorpayClient;
    @Mock private OrderClient orderClient;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() throws RazorpayException {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(paymentService, "keyId", "test_key");
        ReflectionTestUtils.setField(paymentService, "keySecret", "test_secret");
        
        // Mock the nested razorpay.orders
        ReflectionTestUtils.setField(paymentService, "razorpay", razorpayClient);
        razorpayClient.orders = orderClient;
    }

    @Test
    void testCreateOrder_Success() throws RazorpayException {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setPlanName("MONTHLY");

        Order mockOrder = mock(Order.class);
        when(mockOrder.get("id")).thenReturn("order_123");
        when(orderClient.create(any(JSONObject.class))).thenReturn(mockOrder);

        OrderResponse response = paymentService.createOrder(request);

        assertNotNull(response);
        assertEquals("order_123", response.getId());
        assertEquals(new BigDecimal("199"), response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals("test_key", response.getKeyId());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCreateOrder_YearlyPlanUsesYearlyAmount() throws RazorpayException {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(2L);
        request.setPlanName("YEARLY");

        Order mockOrder = mock(Order.class);
        when(mockOrder.get("id")).thenReturn("order_yearly");
        when(orderClient.create(any(JSONObject.class))).thenReturn(mockOrder);

        OrderResponse response = paymentService.createOrder(request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        assertEquals("order_yearly", response.getId());
        assertEquals(new BigDecimal("1499"), response.getAmount());
        assertEquals(new BigDecimal("1499"), paymentCaptor.getValue().getAmount());
        assertEquals(Payment.PaymentStatus.PENDING, paymentCaptor.getValue().getStatus());
        assertEquals("YEARLY", paymentCaptor.getValue().getPlanName());
    }

    @Test
    void testCreateOrder_InvalidPlan() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setPlanName("INVALID");

        assertThrows(IllegalArgumentException.class, () -> paymentService.createOrder(request));
    }

    @Test
    void testCreateOrder_RazorpayFailurePropagates() throws RazorpayException {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setPlanName("MONTHLY");

        when(orderClient.create(any(JSONObject.class))).thenThrow(new RazorpayException("Razorpay unavailable"));

        RazorpayException exception = assertThrows(RazorpayException.class, () -> paymentService.createOrder(request));

        assertEquals("Razorpay unavailable", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testVerifyPayment_Success() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setUserId(1L);
        request.setRazorpayOrderId("order_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("sig_123");

        Payment payment = Payment.builder()
                .razorpayOrderId("order_123")
                .planName("MONTHLY")
                .status(Payment.PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByRazorpayOrderId("order_123")).thenReturn(Optional.of(payment));

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(JSONObject.class), eq("test_secret")))
                    .thenReturn(true);

            boolean result = paymentService.verifyPayment(request);

            assertTrue(result);
            assertEquals(Payment.PaymentStatus.SUCCESS, payment.getStatus());
            assertEquals("pay_123", payment.getRazorpayPaymentId());
            assertEquals("sig_123", payment.getRazorpaySignature());
            verify(paymentRepository).save(payment);
            verify(authClient).upgradeUserPlan(1L, "PRO", 1);
        }
    }

    @Test
    void testVerifyPayment_SuccessForYearlyPlanUsesTwelveMonths() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setUserId(3L);
        request.setRazorpayOrderId("order_yearly");
        request.setRazorpayPaymentId("pay_yearly");
        request.setRazorpaySignature("sig_yearly");

        Payment payment = Payment.builder()
                .razorpayOrderId("order_yearly")
                .planName("YEARLY")
                .status(Payment.PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByRazorpayOrderId("order_yearly")).thenReturn(Optional.of(payment));

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(JSONObject.class), eq("test_secret")))
                    .thenReturn(true);

            boolean result = paymentService.verifyPayment(request);

            assertTrue(result);
            verify(authClient).upgradeUserPlan(3L, "PRO", 12);
        }
    }

    @Test
    void testVerifyPayment_PlanUpgradeFailureStillReturnsTrue() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setUserId(4L);
        request.setRazorpayOrderId("order_partial");
        request.setRazorpayPaymentId("pay_partial");
        request.setRazorpaySignature("sig_partial");

        Payment payment = Payment.builder()
                .razorpayOrderId("order_partial")
                .planName("MONTHLY")
                .status(Payment.PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByRazorpayOrderId("order_partial")).thenReturn(Optional.of(payment));
        doThrow(new RuntimeException("auth service down"))
                .when(authClient).upgradeUserPlan(4L, "PRO", 1);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(JSONObject.class), eq("test_secret")))
                    .thenReturn(true);

            boolean result = paymentService.verifyPayment(request);

            assertTrue(result);
            assertEquals(Payment.PaymentStatus.SUCCESS, payment.getStatus());
            verify(paymentRepository).save(payment);
        }
    }

    @Test
    void testVerifyPayment_InvalidSignatureReturnsFalse() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setUserId(1L);
        request.setRazorpayOrderId("order_invalid");
        request.setRazorpayPaymentId("pay_invalid");
        request.setRazorpaySignature("sig_invalid");

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(JSONObject.class), eq("test_secret")))
                    .thenReturn(false);

            boolean result = paymentService.verifyPayment(request);

            assertFalse(result);
            verify(paymentRepository, never()).findByRazorpayOrderId(anyString());
            verify(paymentRepository, never()).save(any(Payment.class));
            verifyNoInteractions(authClient);
        }
    }

    @Test
    void testVerifyPayment_NotFound() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setUserId(1L);
        request.setRazorpayOrderId("order_not_found");
        request.setRazorpayPaymentId("pay_missing");
        request.setRazorpaySignature("sig_missing");

        when(paymentRepository.findByRazorpayOrderId(anyString())).thenReturn(Optional.empty());

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(JSONObject.class), eq("test_secret")))
                    .thenReturn(true);

            boolean result = paymentService.verifyPayment(request);

            assertFalse(result);
            verify(paymentRepository, never()).save(any(Payment.class));
            verifyNoInteractions(authClient);
        }
    }

    @Test
    void testVerifyPayment_SignatureVerificationThrowsReturnsFalse() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setUserId(1L);
        request.setRazorpayOrderId("order_error");
        request.setRazorpayPaymentId("pay_error");
        request.setRazorpaySignature("sig_error");

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifyPaymentSignature(any(JSONObject.class), eq("test_secret")))
                    .thenThrow(new RuntimeException("signature failure"));

            boolean result = paymentService.verifyPayment(request);

            assertFalse(result);
            verifyNoInteractions(paymentRepository);
            verifyNoInteractions(authClient);
        }
    }
}
