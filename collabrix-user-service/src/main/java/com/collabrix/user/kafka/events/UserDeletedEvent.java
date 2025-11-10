package com.collabrix.user.kafka.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeletedEvent {
    private String eventId;
    private String eventType;
    private Long timestamp;
    private String keycloakUserId;
    private String username;
    private String deletedBy;
}