package com.spendsmart.recurring.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationEventTest {

    @Test
    void inheritedAccessorsShouldWork() {
        NotificationEvent event = new NotificationEvent();
        event.setRecipientId(21L);
        event.setType("RECURRING_DUE");
        event.setSeverity("WARNING");
        event.setTitle("Recurring due");
        event.setMessage("Subscription renews tomorrow");
        event.setRelatedId(45L);
        event.setRelatedType("RECURRING");

        assertInstanceOf(com.spendsmart.shared.events.NotificationEvent.class, event);
        assertEquals(21L, event.getRecipientId());
        assertEquals("RECURRING_DUE", event.getType());
        assertEquals("WARNING", event.getSeverity());
        assertEquals("Recurring due", event.getTitle());
        assertEquals("Subscription renews tomorrow", event.getMessage());
        assertEquals(45L, event.getRelatedId());
        assertEquals("RECURRING", event.getRelatedType());
        assertTrue(event.toString().contains("Recurring due"));
    }
}
