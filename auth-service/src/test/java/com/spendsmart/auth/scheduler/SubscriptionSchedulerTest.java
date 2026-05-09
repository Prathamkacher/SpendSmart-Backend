package com.spendsmart.auth.scheduler;

import com.spendsmart.auth.constants.AppConstants;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SubscriptionScheduler subscriptionScheduler;

    private User proUser;

    @BeforeEach
    void setUp() {
        proUser = User.builder()
                .userId(1L)
                .email("user@example.com")
                .fullName("User One")
                .planType(User.PlanType.PRO)
                .build();
    }

    @Test
    void checkExpiries_ShouldSendThreeDayReminder() {
        proUser.setPlanExpiryDate(LocalDateTime.now().plusDays(3).plusHours(1));
        when(userRepository.findByPlanTypeIn(any())).thenReturn(List.of(proUser));

        subscriptionScheduler.checkExpiries();

        verify(rabbitTemplate).convertAndSend(eq(AppConstants.NOTIFICATION_EXCHANGE), eq(AppConstants.NOTIFICATION_ROUTING_KEY), any(Object.class));
        verify(userRepository, never()).save(any());
    }

    @Test
    void checkExpiries_ShouldDowngradeExpiredUsers() {
        proUser.setPlanExpiryDate(LocalDateTime.now().minusHours(1));
        proUser.setPlanStartDate(LocalDateTime.now().minusMonths(1));
        when(userRepository.findByPlanTypeIn(any())).thenReturn(List.of(proUser));

        subscriptionScheduler.checkExpiries();

        assertThat(proUser.getPlanType()).isEqualTo(User.PlanType.FREE);
        assertThat(proUser.getPlanExpiryDate()).isNull();
        assertThat(proUser.getPlanStartDate()).isNull();
        verify(userRepository).save(proUser);
    }

    @Test
    void checkExpiries_ShouldIgnoreUsersWithoutExpiry() {
        when(userRepository.findByPlanTypeIn(any())).thenReturn(List.of(proUser));

        subscriptionScheduler.checkExpiries();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void checkExpiries_ShouldSwallowNotificationPublishingErrors() {
        proUser.setPlanExpiryDate(LocalDateTime.now().plusDays(1).plusHours(1));
        when(userRepository.findByPlanTypeIn(any())).thenReturn(List.of(proUser));
        doThrow(new RuntimeException("Broker down")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(Object.class));

        subscriptionScheduler.checkExpiries();

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
