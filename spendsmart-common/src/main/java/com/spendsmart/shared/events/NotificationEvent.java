package com.spendsmart.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private Long recipientId;
    private String type;
    private String severity;
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType;
}
