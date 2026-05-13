package com.spendsmart.budget.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ in the Budget service.
 * Inherits base notification publishing configuration from shared library.
 */
@Configuration
public class RabbitMQConfig extends com.spendsmart.shared.amqp.NotificationPublisherRabbitConfig {
}
