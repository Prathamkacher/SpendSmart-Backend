package com.spendsmart.notification;

import com.spendsmart.notification.config.RabbitMQConfig;
import com.spendsmart.notification.dto.NotificationDTO;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.dto.UserProfileResponse;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.shared.events.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class SupportClassesTest {

    @Test
    void main_ShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            NotificationServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});

            springApplication.verify(() ->
                    SpringApplication.run(NotificationServiceApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }

    @Test
    void rabbitConfig_ShouldCreateExpectedArtifacts() {
        RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

        Queue queue = rabbitMQConfig.notificationQueue();
        TopicExchange exchange = rabbitMQConfig.notificationExchange();
        Binding binding = rabbitMQConfig.binding(queue, exchange);
        Object converter = rabbitMQConfig.converter();
        AmqpTemplate template = rabbitMQConfig.template(mock(ConnectionFactory.class));

        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.NOTIFICATION_QUEUE);
        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.NOTIFICATION_EXCHANGE);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.NOTIFICATION_ROUTING_KEY);
        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
        assertThat(template).isInstanceOf(RabbitTemplate.class);
    }

    @Test
    void supportDtosAndEntity_ShouldRetainValuesAndDefaults() {
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(1L)
                .type("SYSTEM")
                .severity("INFO")
                .title("Hello")
                .message("World")
                .relatedId(2L)
                .relatedType("USER")
                .build();
        NotificationRequest request = NotificationRequest.builder()
                .recipientId(1L)
                .type(Notification.NotificationType.SYSTEM)
                .severity(Notification.Severity.INFO)
                .title("Hello")
                .message("World")
                .relatedId(2L)
                .relatedType("USER")
                .build();
        NotificationDTO dto = NotificationDTO.builder()
                .notificationId(10L)
                .recipientId(1L)
                .type(Notification.NotificationType.SYSTEM)
                .severity(Notification.Severity.WARNING)
                .title("Alert")
                .message("Pay attention")
                .relatedId(3L)
                .relatedType("BUDGET")
                .isRead(true)
                .isAcknowledged(false)
                .createdAt(LocalDateTime.now())
                .build();
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId(7L)
                .email("user@example.com")
                .fullName("User Name")
                .currency("INR")
                .build();
        Notification notification = Notification.builder()
                .notificationId(11L)
                .recipientId(1L)
                .type(Notification.NotificationType.SYSTEM)
                .severity(Notification.Severity.INFO)
                .title("Created")
                .message("Created message")
                .build();
        NotificationEvent mutableEvent = new NotificationEvent();
        mutableEvent.setRecipientId(5L);
        mutableEvent.setType("BUDGET_ALERT");
        mutableEvent.setSeverity("WARNING");
        mutableEvent.setTitle("Budget warning");
        mutableEvent.setMessage("Threshold reached");
        mutableEvent.setRelatedId(88L);
        mutableEvent.setRelatedType("BUDGET");
        invokeOnCreate(notification);

        assertThat(event.getRelatedType()).isEqualTo("USER");
        assertThat(request.getSeverity()).isEqualTo(Notification.Severity.INFO);
        assertThat(dto.isRead()).isTrue();
        assertThat(profile.getCurrency()).isEqualTo("INR");
        assertThat(mutableEvent.getTitle()).isEqualTo("Budget warning");
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.isAcknowledged()).isFalse();
        assertThat(notification.isDeleted()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
    }

    private static void invokeOnCreate(Notification notification) {
        try {
            java.lang.reflect.Method method = Notification.class.getDeclaredMethod("onCreate");
            method.setAccessible(true);
            method.invoke(notification);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
