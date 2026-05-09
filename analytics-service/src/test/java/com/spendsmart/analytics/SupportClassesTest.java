package com.spendsmart.analytics;

import com.spendsmart.analytics.config.FeignConfig;
import com.spendsmart.analytics.config.RabbitMQConfig;
import com.spendsmart.analytics.dto.MonthlySummary;
import com.spendsmart.analytics.exception.GlobalExceptionHandler;
import com.spendsmart.analytics.service.AnalyticsService;
import com.spendsmart.analytics.service.ReportService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.spendsmart.shared.dto.ApiResponse;
import com.spendsmart.shared.events.NotificationEvent;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SupportClassesTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void main_ShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            AnalyticsServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});

            springApplication.verify(() ->
                    SpringApplication.run(AnalyticsServiceApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }

    @Test
    void requestInterceptor_ShouldPropagateAuthorizationHeaderWhenPresent() {
        RequestInterceptor interceptor = new FeignConfig().requestInterceptor();
        RequestTemplate template = new RequestTemplate();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer analytics-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        interceptor.apply(template);

        assertThat(template.headers()).containsKey("Authorization");
        assertThat(template.headers().get("Authorization")).containsExactly("Bearer analytics-token");
    }

    @Test
    void requestInterceptor_ShouldIgnoreMissingHeaderOrRequestContext() {
        RequestInterceptor interceptor = new FeignConfig().requestInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);
        assertThat(template.headers()).doesNotContainKey("Authorization");

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    @Test
    void globalExceptionHandler_ShouldWrapRuntimeException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(new RuntimeException("analytics failure"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("RuntimeException").contains("analytics failure");
    }

    @Test
    void globalExceptionHandler_ShouldDescribeGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(new Exception("unexpected"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("Exception").contains("unexpected");
    }

    @Test
    void supportDtosAndRabbitConfig_ShouldExposeExpectedValues() {
        ApiResponse<String> success = ApiResponse.success("ok", "payload");
        ApiResponse<Void> successWithoutData = ApiResponse.success("ok");
        ApiResponse<Void> error = ApiResponse.error("failed");
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .recipientId(1L)
                .type("SYSTEM")
                .severity("INFO")
                .title("Hello")
                .message("World")
                .relatedId(2L)
                .relatedType("USER")
                .build();
        NotificationEvent mutableNotificationEvent = new NotificationEvent();
        mutableNotificationEvent.setRecipientId(5L);
        mutableNotificationEvent.setType("SYSTEM");
        mutableNotificationEvent.setSeverity("WARNING");
        mutableNotificationEvent.setTitle("Heads up");
        mutableNotificationEvent.setMessage("Review analytics");
        mutableNotificationEvent.setRelatedId(6L);
        mutableNotificationEvent.setRelatedType("REPORT");
        RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();
        RabbitTemplate template = rabbitMQConfig.rabbitTemplate(mock(ConnectionFactory.class));

        assertThat(success.isSuccess()).isTrue();
        assertThat(success.getData()).isEqualTo("payload");
        assertThat(successWithoutData.isSuccess()).isTrue();
        assertThat(successWithoutData.getData()).isNull();
        assertThat(error.isSuccess()).isFalse();
        assertThat(notificationEvent.getRecipientId()).isEqualTo(1L);
        assertThat(notificationEvent.getType()).isEqualTo("SYSTEM");
        assertThat(notificationEvent.getSeverity()).isEqualTo("INFO");
        assertThat(notificationEvent.getTitle()).isEqualTo("Hello");
        assertThat(notificationEvent.getMessage()).isEqualTo("World");
        assertThat(notificationEvent.getRelatedId()).isEqualTo(2L);
        assertThat(notificationEvent.getRelatedType()).isEqualTo("USER");
        assertThat(mutableNotificationEvent.getRecipientId()).isEqualTo(5L);
        assertThat(mutableNotificationEvent.getType()).isEqualTo("SYSTEM");
        assertThat(mutableNotificationEvent.getSeverity()).isEqualTo("WARNING");
        assertThat(mutableNotificationEvent.getTitle()).isEqualTo("Heads up");
        assertThat(mutableNotificationEvent.getMessage()).isEqualTo("Review analytics");
        assertThat(mutableNotificationEvent.getRelatedId()).isEqualTo(6L);
        assertThat(mutableNotificationEvent.getRelatedType()).isEqualTo("REPORT");
        assertThat(rabbitMQConfig.notificationExchange().getName()).isEqualTo(RabbitMQConfig.NOTIFICATION_EXCHANGE);
        assertThat(rabbitMQConfig.converter()).isInstanceOf(Jackson2JsonMessageConverter.class);
        assertThat(template.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    @Test
    void reportService_ShouldGenerateCsvWithBreakdownRows() {
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        MonthlySummary summary = new MonthlySummary();
        summary.setTotalIncome(new BigDecimal("5000.00"));
        summary.setTotalExpenses(new BigDecimal("3200.00"));
        summary.setNetSavings(new BigDecimal("1800.00"));
        summary.setSavingsRate(new BigDecimal("36.00"));

        when(analyticsService.getMonthlySummary(1L, 2026, 5)).thenReturn(summary);
        when(analyticsService.getExpenseBreakdownByCategory(1L, 2026, 5))
                .thenReturn(java.util.Map.of("Food", 1200.0, "Travel", 800.0));

        String csv = new ReportService(analyticsService).generateMonthlyReportCsv(1L, 2026, 5);

        assertThat(csv)
                .contains("SpendSmart Financial Report")
                .contains("Total Income,5000.00")
                .contains("Food,1200.00")
                .contains("Travel,800.00");
    }

    @Test
    void reportService_ShouldDescribeMissingBreakdown() {
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        MonthlySummary summary = new MonthlySummary();
        summary.setTotalIncome(BigDecimal.ZERO);
        summary.setTotalExpenses(BigDecimal.ZERO);
        summary.setNetSavings(BigDecimal.ZERO);
        summary.setSavingsRate(BigDecimal.ZERO);

        when(analyticsService.getMonthlySummary(2L, 2026, 4)).thenReturn(summary);
        when(analyticsService.getExpenseBreakdownByCategory(2L, 2026, 4)).thenReturn(java.util.Collections.emptyMap());

        String csv = new ReportService(analyticsService).generateMonthlyReportCsv(2L, 2026, 4);

        assertThat(csv)
                .contains("No expenses recorded for this month.")
                .contains("Period: 04/2026");
    }
}
