package com.collabrix.common.libraries.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user's role is changed (assigned/removed).
 * This event will be consumed by:
 * 1. user-service: to update user role in PostgreSQL
 * 2. notification-service: to notify user about role change
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserRoleChangedEvent extends BaseEvent {
    private String keycloakUserId;
    private String username;
    private String roleName;
    private String action;          // "ASSIGNED" or "REMOVED"
    private String changedBy;       // Admin username who made the change
}