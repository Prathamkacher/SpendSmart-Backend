package com.spendsmart.shared.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEventTest {

    @Test
    void builderShouldPopulateAllFields() {
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(42L)
                .type("PAYMENT")
                .severity("HIGH")
                .title("Payment received")
                .message("Premium plan activated")
                .relatedId(99L)
                .relatedType("SUBSCRIPTION")
                .build();

        assertThat(event.getRecipientId()).isEqualTo(42L);
        assertThat(event.getType()).isEqualTo("PAYMENT");
        assertThat(event.getSeverity()).isEqualTo("HIGH");
        assertThat(event.getTitle()).isEqualTo("Payment received");
        assertThat(event.getMessage()).isEqualTo("Premium plan activated");
        assertThat(event.getRelatedId()).isEqualTo(99L);
        assertThat(event.getRelatedType()).isEqualTo("SUBSCRIPTION");
    }

    @Test
    void settersAndEqualityShouldWork() {
        NotificationEvent first = new NotificationEvent();
        first.setRecipientId(7L);
        first.setType("ALERT");
        first.setSeverity("LOW");
        first.setTitle("Reminder");
        first.setMessage("Budget threshold reached");
        first.setRelatedId(12L);
        first.setRelatedType("BUDGET");

        NotificationEvent second = new NotificationEvent(7L, "ALERT", "LOW", "Reminder",
                "Budget threshold reached", 12L, "BUDGET");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first.toString()).contains("Reminder");
    }
}
