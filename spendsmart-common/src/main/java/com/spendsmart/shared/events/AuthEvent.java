package com.spendsmart.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthEvent {

    public enum EventType { USER_REGISTERED, USER_LOGGED_IN, USER_DEACTIVATED }

    private EventType eventType;
    private Long userId;
    private String email;
    private String fullName;

    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();
}
