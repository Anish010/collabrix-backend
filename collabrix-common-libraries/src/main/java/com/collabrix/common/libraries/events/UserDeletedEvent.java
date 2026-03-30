package com.collabrix.common.libraries.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Event published when a user is deleted (soft/hard delete).
 * This event will be consumed by:
 * 1. user-service: to mark user as deleted in PostgreSQL
 * 2. notification-service: to send account deletion confirmation
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserDeletedEvent extends BaseEvent {
    private String keycloakUserId;
    private String username;
    private String deletedBy;       // Admin username who deleted the user
}