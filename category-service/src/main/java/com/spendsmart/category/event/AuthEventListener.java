package com.spendsmart.category.event;

import com.spendsmart.category.config.RabbitMQConfig;
import com.spendsmart.category.service.CategoryService;
import com.spendsmart.shared.events.AuthEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final CategoryService categoryService;

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE)
    public void handleAuthEvent(AuthEvent event) {
        log.info("Received AuthEvent: {} for user: {} ({})",
                event.getEventType(), event.getFullName(), event.getEmail());

        if (event.getEventType() == AuthEvent.EventType.USER_REGISTERED) {
            handleUserRegistration(event);
        } else if (event.getEventType() == AuthEvent.EventType.USER_DEACTIVATED) {
            handleUserDeactivation(event);
        }
    }

    private void handleUserRegistration(AuthEvent event) {
        log.info("Seeding default categories for new user: {}", event.getUserId());
        try {
            categoryService.initDefaultCategories(event.getUserId());
            log.info("Default categories seeded successfully for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to seed default categories for user {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }

    private void handleUserDeactivation(AuthEvent event) {
        log.info("User deactivated event received for user: {}. No cleanup action configured.", event.getUserId());
    }
}
