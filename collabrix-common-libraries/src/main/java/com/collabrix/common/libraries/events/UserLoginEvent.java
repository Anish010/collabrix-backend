package com.collabrix.common.libraries.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user is logged in).
 * This event will be consumed by:
 * 1. user-service: to mark user as active
 * 2. notification-service: to send logout confirmation
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserLoginEvent extends BaseEvent {
    private String keycloakUserId;
    private String email;
    private String ipAddress;
    private String userAgent;
}
