package com.collabrix.common.libraries.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user is logged out).
 * This event will be consumed by:
 * 1. user-service: to mark user as inactive
 * 2. notification-service: to send logout confirmation
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserLogoutEvent extends BaseEvent {
    private String keycloakUserId;
    private String refreshToken; // optional, but useful for audit
}
