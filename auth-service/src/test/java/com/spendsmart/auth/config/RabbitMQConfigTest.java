package com.spendsmart.auth.config;

import com.spendsmart.auth.constants.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
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
    void beanMethods_ShouldCreateExpectedRabbitArtifacts() {
        TopicExchange exchange = rabbitMQConfig.authExchange();
        Queue queue = rabbitMQConfig.authQueue();
        Binding binding = rabbitMQConfig.binding(queue, exchange);
        MessageConverter converter = rabbitMQConfig.converter();
        AmqpTemplate template = rabbitMQConfig.template(mock(ConnectionFactory.class));

        assertThat(exchange.getName()).isEqualTo(AppConstants.AUTH_EXCHANGE);
        assertThat(queue.getName()).isEqualTo(AppConstants.AUTH_QUEUE);
        assertThat(binding.getRoutingKey()).isEqualTo(AppConstants.AUTH_ROUTING_KEY);
        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
        assertThat(template).isInstanceOf(RabbitTemplate.class);
    }
}
