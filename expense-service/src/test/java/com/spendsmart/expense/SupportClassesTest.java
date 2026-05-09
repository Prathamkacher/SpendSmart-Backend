package com.spendsmart.expense;

import com.spendsmart.expense.client.fallback.BudgetServiceFallback;
import com.spendsmart.expense.client.fallback.CategoryServiceFallback;
import com.spendsmart.expense.config.FeignConfig;
import com.spendsmart.expense.config.RabbitMQConfig;
import com.spendsmart.expense.config.SwaggerConfig;
import com.spendsmart.expense.dto.BudgetUpdateRequest;
import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.dto.ExpenseResponse;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.entity.PaymentMethod;
import com.spendsmart.shared.events.AuthEvent;
import com.spendsmart.expense.event.AuthEventListener;
import com.spendsmart.expense.exception.ExpenseNotFoundException;
import com.spendsmart.shared.dto.ApiResponse;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class SupportClassesTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void main_ShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ExpenseServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});

            springApplication.verify(() ->
                    SpringApplication.run(ExpenseServiceApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }

    @Test
    void supportDtosAndEntity_ShouldRetainValuesAndDefaults() {
        ApiResponse<String> success = ApiResponse.success("saved", "expense");
        ApiResponse<Void> error = ApiResponse.error("failed");
        BudgetUpdateRequest budgetUpdateRequest = BudgetUpdateRequest.builder()
                .userId(3L)
                .categoryId(4L)
                .amount(new BigDecimal("22.50"))
                .build();
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .title("Lunch")
                .amount(new BigDecimal("12.40"))
                .currency("INR")
                .categoryId(5L)
                .type(ExpenseType.EXPENSE)
                .paymentMethod(PaymentMethod.UPI)
                .date(LocalDate.now())
                .notes("Office lunch")
                .receiptUrl("receipt")
                .isRecurring(false)
                .build();
        ExpenseResponse expenseResponse = ExpenseResponse.builder()
                .expenseId(1L)
                .userId(2L)
                .categoryId(5L)
                .title("Lunch")
                .amount(new BigDecimal("12.40"))
                .currency("INR")
                .type(ExpenseType.EXPENSE)
                .paymentMethod(PaymentMethod.UPI)
                .date(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Expense expense = Expense.builder()
                .expenseId(1L)
                .userId(2L)
                .categoryId(3L)
                .title("Groceries")
                .amount(new BigDecimal("750"))
                .date(LocalDate.now())
                .build();

        assertThat(success.isSuccess()).isTrue();
        assertThat(success.getTimestamp()).isNotNull();
        assertThat(error.getMessage()).isEqualTo("failed");
        assertThat(budgetUpdateRequest.getAmount()).isEqualByComparingTo("22.50");
        assertThat(expenseRequest.getPaymentMethod()).isEqualTo(PaymentMethod.UPI);
        assertThat(expenseResponse.getExpenseId()).isEqualTo(1L);
        assertThat(expense.getCurrency()).isEqualTo("INR");
        assertThat(expense.getType()).isEqualTo(ExpenseType.EXPENSE);
        assertThat(expense.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(expense.getIsRecurring()).isFalse();
    }

    @Test
    void authEventAndListener_ShouldHandleAllSupportedEventTypes() {
        AuthEventListener listener = new AuthEventListener();
        AuthEvent registered = new AuthEvent();
        registered.setEventType(AuthEvent.EventType.USER_REGISTERED);
        registered.setUserId(7L);
        registered.setEmail("new@example.com");
        registered.setFullName("New User");
        AuthEvent deactivated = new AuthEvent();
        deactivated.setEventType(AuthEvent.EventType.USER_DEACTIVATED);
        deactivated.setUserId(7L);
        deactivated.setEmail("gone@example.com");
        deactivated.setFullName("Gone User");
        AuthEvent loggedIn = new AuthEvent();
        loggedIn.setEventType(AuthEvent.EventType.USER_LOGGED_IN);
        loggedIn.setUserId(7L);
        loggedIn.setEmail("login@example.com");
        loggedIn.setFullName("Login User");
        AuthEvent mutableEvent = new AuthEvent();
        mutableEvent.setEventType(AuthEvent.EventType.USER_LOGGED_IN);
        mutableEvent.setUserId(8L);
        mutableEvent.setEmail("mutable@example.com");
        mutableEvent.setFullName("Mutable User");

        listener.handleAuthEvent(registered);
        listener.handleAuthEvent(deactivated);
        listener.handleAuthEvent(loggedIn);

        assertThat(registered.getOccurredAt()).isNotNull();
        assertThat(deactivated.getEventType()).isEqualTo(AuthEvent.EventType.USER_DEACTIVATED);
        assertThat(loggedIn.getEmail()).isEqualTo("login@example.com");
        assertThat(mutableEvent.getFullName()).isEqualTo("Mutable User");
    }

    @Test
    void feignInterceptor_ShouldOnlyForwardBearerAuthorizationHeader() {
        RequestInterceptor interceptor = new FeignConfig().authorizationForwardInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);
        assertThat(template.headers()).doesNotContainKey("Authorization");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token abc");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        interceptor.apply(template);
        assertThat(template.headers()).doesNotContainKey("Authorization");

        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        interceptor.apply(template);
        assertThat(template.headers().get("Authorization")).containsExactly("Bearer abc");
    }

    @Test
    void fallbackClients_ShouldReturnSafeResponses() {
        BudgetServiceFallback budgetServiceFallback = new BudgetServiceFallback();
        CategoryServiceFallback categoryServiceFallback = new CategoryServiceFallback();

        budgetServiceFallback.updateSpentAmount(BudgetUpdateRequest.builder()
                .userId(9L)
                .categoryId(10L)
                .amount(new BigDecimal("12"))
                .build());

        ApiResponse<Map<Long, String>> namesResponse = categoryServiceFallback.getCategoryNames();
        ApiResponse<Map<String, Object>> categoryResponse = categoryServiceFallback.getCategoryById(4L);

        assertThat(namesResponse.isSuccess()).isFalse();
        assertThat(namesResponse.getData()).isEmpty();
        assertThat(categoryResponse.isSuccess()).isFalse();
        assertThat(categoryResponse.getData()).isEmpty();
    }

    @Test
    void expenseNotFoundException_ShouldExposeHelpfulMessage() {
        assertThat(new ExpenseNotFoundException("missing").getMessage()).isEqualTo("missing");
        assertThat(new ExpenseNotFoundException(44L).getMessage()).contains("44");
    }

    @Test
    void configBeans_ShouldExposeExpectedMetadata() {
        RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();
        OpenAPI openAPI = new SwaggerConfig().expenseServiceOpenAPI();

        assertThat(rabbitMQConfig.authQueue().getName()).isEqualTo(RabbitMQConfig.AUTH_QUEUE);
        assertThat(rabbitMQConfig.converter()).isInstanceOf(Jackson2JsonMessageConverter.class);
        assertThat(openAPI.getInfo().getTitle()).contains("Expense Service");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("BearerAuth");
    }
}
