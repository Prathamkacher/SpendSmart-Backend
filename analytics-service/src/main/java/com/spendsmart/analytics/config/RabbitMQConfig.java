package com.spendsmart.analytics.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ in the Analytics service.
 * Inherits base notification publishing configuration from shared library.
 */
@Configuration
public class RabbitMQConfig extends com.spendsmart.shared.amqp.NotificationPublisherRabbitConfig {
}
