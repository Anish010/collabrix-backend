package com.collabrix.auth.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a user is deleted (soft/hard delete).
 * This event will be consumed by:
 * 1. user-service: to mark user as deleted in PostgreSQL
 * 2. notification-service: to send account deletion confirmation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent {

    private String eventId;
    private String eventType = "USER_DELETED";
    private Long timestamp;

    private String keycloakUserId;
    private String username;
    private String deletedBy;       // Admin username who deleted the user
}