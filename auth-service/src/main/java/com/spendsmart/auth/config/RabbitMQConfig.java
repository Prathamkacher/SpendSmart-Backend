package com.spendsmart.auth.config;

import com.spendsmart.auth.constants.AppConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ messaging.
 * Defines exchanges, queues, and bindings for authentication-related events.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AppConstants.AUTH_EXCHANGE);
    }

    @Bean
    public Queue authQueue() {
        return new Queue(AppConstants.AUTH_QUEUE, true);
    }

    @Bean
    public Binding binding(Queue authQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with(AppConstants.AUTH_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
