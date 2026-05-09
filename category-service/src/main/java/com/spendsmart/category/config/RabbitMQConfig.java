package com.spendsmart.category.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String AUTH_QUEUE = "auth.queue";

    @Bean
    public org.springframework.amqp.core.Queue authQueue() {
        return new org.springframework.amqp.core.Queue(AUTH_QUEUE, true);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}
