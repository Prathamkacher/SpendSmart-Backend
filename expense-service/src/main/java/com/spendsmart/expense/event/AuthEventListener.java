package com.spendsmart.expense.event;

import com.spendsmart.expense.config.RabbitMQConfig;
import com.spendsmart.shared.events.AuthEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthEventListener {

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
        log.info("Seeding default data for new user: {}", event.getUserId());
        // Default expense data is currently seeded by the owning service workflow.
    }

    private void handleUserDeactivation(AuthEvent event) {
        log.info("Cleaning up data for deactivated user: {}", event.getUserId());
        // Expense history is retained intentionally; no local cleanup is required here.
    }
}
