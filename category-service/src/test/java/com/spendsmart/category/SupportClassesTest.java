package com.spendsmart.category;

import com.spendsmart.category.config.RabbitMQConfig;
import com.spendsmart.shared.events.AuthEvent;
import com.spendsmart.category.event.AuthEventListener;
import com.spendsmart.category.service.CategoryService;
import com.spendsmart.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class SupportClassesTest {

    @Test
    void main_ShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            CategoryServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});

            springApplication.verify(() ->
                    SpringApplication.run(CategoryServiceApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }

    @Test
    void apiResponseFactoriesAndConstructors_ShouldPopulateExpectedValues() {
        ApiResponse<String> success = ApiResponse.success("saved", "category");
        ApiResponse<Void> successWithoutData = ApiResponse.success("ok");
        ApiResponse<Void> error = ApiResponse.error("failed");
        assertThat(success.isSuccess()).isTrue();
        assertThat(success.getData()).isEqualTo("category");
        assertThat(success.getTimestamp()).isNotNull();
        assertThat(successWithoutData.isSuccess()).isTrue();
        assertThat(successWithoutData.getData()).isNull();
        assertThat(error.isSuccess()).isFalse();
        assertThat(error.getMessage()).isEqualTo("failed");
    }

    @Test
    void rabbitConfig_ShouldExposeQueueAndJsonConverter() {
        RabbitMQConfig config = new RabbitMQConfig();

        assertThat(config.authQueue().getName()).isEqualTo(RabbitMQConfig.AUTH_QUEUE);
        assertThat(config.authQueue().isDurable()).isTrue();
        assertThat(config.converter()).isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    @Test
    void authEventAndListener_ShouldHandleRegistrationAndIgnoreOtherEvents() {
        CategoryService categoryService = mock(CategoryService.class);
        AuthEventListener listener = new AuthEventListener(categoryService);
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

        listener.handleAuthEvent(registered);
        listener.handleAuthEvent(deactivated);
        listener.handleAuthEvent(loggedIn);

        verify(categoryService).initDefaultCategories(7L);
        assertThat(registered.getOccurredAt()).isNotNull();
        assertThat(deactivated.getEmail()).isEqualTo("gone@example.com");
        assertThat(loggedIn.getFullName()).isEqualTo("Login User");
    }
}
