package com.spendsmart.budget.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private final RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

    @Test
    void beans_ShouldBeConfiguredWithExpectedExchangeAndConverter() {
        TopicExchange exchange = rabbitMQConfig.notificationExchange();
        MessageConverter converter = rabbitMQConfig.converter();
        RabbitTemplate rabbitTemplate = rabbitMQConfig.rabbitTemplate(mock(ConnectionFactory.class));

        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.NOTIFICATION_EXCHANGE);
        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
        assertThat(rabbitTemplate.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
