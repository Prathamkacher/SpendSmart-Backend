package com.spendsmart.category.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ messaging.
 * Sets up queues and message converters for asynchronous communication.
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Name of the queue for authentication-related events.
     */
    public static final String AUTH_QUEUE = "auth.queue";

    /**
     * Defines the authentication queue bean.
     * @return A durable Queue object.
     */
    @Bean
    public org.springframework.amqp.core.Queue authQueue() {
        return new org.springframework.amqp.core.Queue(AUTH_QUEUE, true);
    }

    /**
     * Configures a JSON message converter for RabbitMQ.
     * @return Jackson2JsonMessageConverter bean.
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}
