package com.collabrix.common.libraries.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a new user registers.
 * This event will be consumed by:
 * 1. user-service: to create user profile in PostgreSQL
 * 2. notification-service: to send welcome email
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserRegisteredEvent extends BaseEvent {
    private String keycloakUserId;          // Keycloak user ID
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}