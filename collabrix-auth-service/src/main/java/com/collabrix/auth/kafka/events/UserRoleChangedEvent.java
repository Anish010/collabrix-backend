package com.collabrix.auth.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a user's role is changed (assigned/removed).
 * This event will be consumed by:
 * 1. user-service: to update user role in PostgreSQL
 * 2. notification-service: to notify user about role change
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleChangedEvent {

    private String eventId;
    private String eventType = "USER_ROLE_CHANGED";
    private Long timestamp;

    private String keycloakUserId;
    private String username;
    private String roleName;
    private String action;          // "ASSIGNED" or "REMOVED"
    private String changedBy;       // Admin username who made the change
}