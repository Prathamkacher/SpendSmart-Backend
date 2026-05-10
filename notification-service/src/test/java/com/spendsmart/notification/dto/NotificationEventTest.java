package com.spendsmart.notification.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationEventTest {

    @Test
    void inheritedAccessorsShouldWork() {
        NotificationEvent event = new NotificationEvent();
        event.setRecipientId(12L);
        event.setType("SYSTEM");
        event.setSeverity("INFO");
        event.setTitle("Heads up");
        event.setMessage("Notification created");
        event.setRelatedId(99L);
        event.setRelatedType("USER");

        assertInstanceOf(com.spendsmart.shared.events.NotificationEvent.class, event);
        assertEquals(12L, event.getRecipientId());
        assertEquals("SYSTEM", event.getType());
        assertEquals("INFO", event.getSeverity());
        assertEquals("Heads up", event.getTitle());
        assertEquals("Notification created", event.getMessage());
        assertEquals(99L, event.getRelatedId());
        assertEquals("USER", event.getRelatedType());
        assertTrue(event.toString().contains("Heads up"));
    }
}
