package com.spendsmart.gateway;

import com.spendsmart.gateway.config.SwaggerConfig;
import com.spendsmart.gateway.controller.FallbackController;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.SpringApplication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class SupportClassesTest {

    @Test
    void main_ShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ApiGatewayApplication.main(new String[]{"--spring.main.web-application-type=reactive"});

            springApplication.verify(() ->
                    SpringApplication.run(ApiGatewayApplication.class, new String[]{"--spring.main.web-application-type=reactive"}));
        }
    }

    @Test
    void fallbackEndpoints_ShouldReturnServiceUnavailableMessages() {
        FallbackController controller = new FallbackController();

        assertFallbackMessage(controller.authFallback().block(), "Authentication Service is temporarily unavailable. Please try again later.");
        assertFallbackMessage(controller.expenseFallback().block(), "Expense Service is currently down. Your request will be processed once it is back online.");
        assertFallbackMessage(controller.incomeFallback().block(), "Income Service is currently down. Please try again later.");
        assertFallbackMessage(controller.categoryFallback().block(), "Category Service is currently unavailable.");
        assertFallbackMessage(controller.budgetFallback().block(), "Budget Service is currently unavailable.");
        assertFallbackMessage(controller.analyticsFallback().block(), "Analytics Service is currently unavailable. Reports cannot be generated at this moment.");
        assertFallbackMessage(controller.recurringFallback().block(), "Recurring Transaction Service is currently unavailable.");
        assertFallbackMessage(controller.notificationFallback().block(), "Notification Service is currently unavailable. Notifications may be delayed.");
        assertFallbackMessage(controller.paymentFallback().block(), "Payment Service is currently down. Transactions cannot be processed at this time.");
    }

    @Test
    void swaggerUiConfigProperties_ShouldRegisterGatewayDocs() {
        SwaggerUiConfigProperties properties = new SwaggerConfig().swaggerUiConfigProperties();

        assertThat(properties.getUrls()).hasSize(2);
        assertThat(properties.getUrls())
                .extracting(SwaggerUiConfigProperties.SwaggerUrl::getName)
                .containsExactlyInAnyOrder("Auth Service", "Expense Service");
    }

    private static void assertFallbackMessage(Map<String, String> response, String message) {
        assertThat(response).containsEntry("status", "SERVICE_UNAVAILABLE");
        assertThat(response).containsEntry("message", message);
    }
}
