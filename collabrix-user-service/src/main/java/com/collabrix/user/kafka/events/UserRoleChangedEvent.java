package com.collabrix.user.kafka.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleChangedEvent {
    private String eventId;
    private String eventType;
    private Long timestamp;
    private String keycloakUserId;
    private String username;
    private String roleName;
    private String action; // "ASSIGNED" or "REMOVED"
    private String changedBy;
}