package com.spendsmart.auth.scheduler;

import com.spendsmart.auth.constants.AppConstants;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled task for managing user subscriptions.
 * Checks for plan expirations and sends notifications or downgrades plans automatically.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Runs daily at midnight to check for expiring plans.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkExpiries() {
        log.info("Running daily subscription expiry check...");
        LocalDateTime now = LocalDateTime.now();
        
        List<User> activeProUsers = userRepository.findByPlanTypeIn(List.of(User.PlanType.PRO, User.PlanType.TRIAL));
        
        for (User user : activeProUsers) {
            if (user.getPlanExpiryDate() == null) continue;
            
            long daysLeft = ChronoUnit.DAYS.between(now, user.getPlanExpiryDate());
            
            if (daysLeft == 3) {
                publishSubscriptionEvent(user, "plan.expiring.3days", "Your premium plan expires in 3 days.");
            } else if (daysLeft == 1) {
                publishSubscriptionEvent(user, "plan.expiring.1day", "Your premium plan expires tomorrow. Renew now!");
            } else if (daysLeft <= 0) {
                log.info("Plan expired for user {}. Downgrading via scheduler.", user.getUserId());
                user.setPlanType(User.PlanType.FREE);
                user.setPlanExpiryDate(null);
                user.setPlanStartDate(null);
                userRepository.save(user);
                publishSubscriptionEvent(user, "plan.expired", "Your premium plan has expired. Upgrade to continue using pro features.");
            }
        }
    }

    private void publishSubscriptionEvent(User user, String type, String message) {
        try {
            com.spendsmart.shared.events.NotificationEvent event = com.spendsmart.shared.events.NotificationEvent.builder()
                    .recipientId(user.getUserId())
                    .type("SYSTEM")
                    .severity(type.contains("expired") ? "CRITICAL" : "INFO")
                    .title(type.replace(".", " ").toUpperCase())
                    .message(message)
                    .build();
            
            rabbitTemplate.convertAndSend(AppConstants.NOTIFICATION_EXCHANGE, AppConstants.NOTIFICATION_ROUTING_KEY, event);
            log.info("Published subscription notification: {} for user {}", type, user.getUserId());
        } catch (Exception ex) {
            log.error("Failed to publish subscription event: {}", ex.getMessage());
        }
    }
}
