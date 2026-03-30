package com.collabrix.auth.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Event published when a new user registers.
 * This event will be consumed by:
 * 1. user-service: to create user profile in PostgreSQL
 * 2. notification-service: to send welcome email
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private String keycloakUserId;          // Keycloak user ID
    private String username;
    private String email;

    // Extended profile fields for user-service
    private String firstName;
    private String lastName;
    private String countryCode;
    private String contactNo;
    private String organization;

    private Long timestamp;
    private String eventType;       // "USER_REGISTERED"
    private String eventId;
}