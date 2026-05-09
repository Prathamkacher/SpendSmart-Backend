package com.spendsmart.shared.events;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AuthEventTest {

    @Test
    void testAuthEvent() {
        AuthEvent event = AuthEvent.builder()
                .eventType(AuthEvent.EventType.USER_LOGGED_IN)
                .userId(1L)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        assertThat(event.getEventType()).isEqualTo(AuthEvent.EventType.USER_LOGGED_IN);
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getEmail()).isEqualTo("test@example.com");
        assertThat(event.getFullName()).isEqualTo("Test User");
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void testAuthEventToString() {
        AuthEvent event = AuthEvent.builder()
                .eventType(AuthEvent.EventType.USER_REGISTERED)
                .userId(2L)
                .email("new@example.com")
                .fullName("New User")
                .build();

        String toString = event.toString();
        assertThat(toString).contains("new@example.com");
        assertThat(toString).contains("USER_REGISTERED");
    }
}
