package com.spendsmart.shared.events;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class AuthEventTest {

    @Test
    void testAuthEventBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        AuthEvent event = AuthEvent.builder()
                .eventType(AuthEvent.EventType.USER_LOGGED_IN)
                .userId(1L)
                .email("test@example.com")
                .fullName("Test User")
                .occurredAt(now)
                .build();

        assertThat(event.getEventType()).isEqualTo(AuthEvent.EventType.USER_LOGGED_IN);
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getEmail()).isEqualTo("test@example.com");
        assertThat(event.getFullName()).isEqualTo("Test User");
        assertThat(event.getOccurredAt()).isEqualTo(now);
    }

    @Test
    void testAuthEventSettersAndNoArgsConstructor() {
        AuthEvent event = new AuthEvent();
        LocalDateTime now = LocalDateTime.now();
        
        event.setEventType(AuthEvent.EventType.USER_REGISTERED);
        event.setUserId(2L);
        event.setEmail("new@example.com");
        event.setFullName("New User");
        event.setOccurredAt(now);

        assertThat(event.getEventType()).isEqualTo(AuthEvent.EventType.USER_REGISTERED);
        assertThat(event.getUserId()).isEqualTo(2L);
        assertThat(event.getEmail()).isEqualTo("new@example.com");
        assertThat(event.getFullName()).isEqualTo("New User");
        assertThat(event.getOccurredAt()).isEqualTo(now);
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        AuthEvent event = new AuthEvent(AuthEvent.EventType.USER_DEACTIVATED, 3L, "old@example.com", "Old User", now);
        
        assertThat(event.getEventType()).isEqualTo(AuthEvent.EventType.USER_DEACTIVATED);
        assertThat(event.getUserId()).isEqualTo(3L);
        assertThat(event.getEmail()).isEqualTo("old@example.com");
        assertThat(event.getFullName()).isEqualTo("Old User");
        assertThat(event.getOccurredAt()).isEqualTo(now);
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        AuthEvent event1 = new AuthEvent(AuthEvent.EventType.USER_LOGGED_IN, 1L, "test@example.com", "Test User", now);
        AuthEvent event2 = new AuthEvent(AuthEvent.EventType.USER_LOGGED_IN, 1L, "test@example.com", "Test User", now);
        AuthEvent event3 = new AuthEvent(AuthEvent.EventType.USER_REGISTERED, 2L, "other@example.com", "Other User", now);

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1).isNotEqualTo(event3);
        assertThat(event1.hashCode()).isNotEqualTo(event3.hashCode());
    }

    @Test
    void testToString() {
        AuthEvent event = AuthEvent.builder()
                .eventType(AuthEvent.EventType.USER_REGISTERED)
                .userId(2L)
                .email("new@example.com")
                .fullName("New User")
                .build();

        String toString = event.toString();
        assertThat(toString).contains("new@example.com");
        assertThat(toString).contains("USER_REGISTERED");
        assertThat(toString).contains("New User");
        assertThat(toString).contains("userId=2");
        
        // Cover builder toString
        String builderToString = AuthEvent.builder().email("test").toString();
        assertThat(builderToString).contains("email=test");
    }

    @Test
    void testEnum() {
        AuthEvent.EventType[] values = AuthEvent.EventType.values();
        assertThat(values).contains(AuthEvent.EventType.USER_REGISTERED, AuthEvent.EventType.USER_LOGGED_IN, AuthEvent.EventType.USER_DEACTIVATED);
        
        AuthEvent.EventType value = AuthEvent.EventType.valueOf("USER_LOGGED_IN");
        assertThat(value).isEqualTo(AuthEvent.EventType.USER_LOGGED_IN);
    }
}
