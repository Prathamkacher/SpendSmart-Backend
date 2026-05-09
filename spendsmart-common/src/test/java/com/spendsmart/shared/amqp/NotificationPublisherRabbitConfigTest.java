package com.spendsmart.shared.amqp;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPublisherRabbitConfigTest {

    private final NotificationPublisherRabbitConfig config = new NotificationPublisherRabbitConfig();

    @Test
    void testNotificationExchange() {
        TopicExchange exchange = config.notificationExchange();

        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo(NotificationPublisherRabbitConfig.NOTIFICATION_EXCHANGE);
    }

    @Test
    void testConverter() {
        MessageConverter converter = config.converter();

        assertThat(converter).isNotNull();
        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
