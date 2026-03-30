package com.collabrix.auth.service;

import com.collabrix.auth.dto.RegisterRequest;
import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.kafka.EventPublisher;
import com.collabrix.auth.kafka.events.UserDeletedEvent;
import com.collabrix.auth.kafka.events.UserRegisteredEvent;
import com.collabrix.auth.kafka.events.UserRoleChangedEvent;
import com.collabrix.common.libraries.exceptions.KeycloakException;
import com.collabrix.common.libraries.exceptions.ResourceAlreadyExistsException;
import com.collabrix.common.libraries.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Keycloak user management - ONLY authentication-related operations.
 * Extended profile management is handled by user-service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloakAdmin;
    private final EventPublisher eventPublisher;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Register a new user in Keycloak
     * Extended profile fields are published as event for user-service
     */
    public UserResponse registerUser(RegisterRequest request) {
        log.info("🔒 Registering new user: {}", request.getUsername());

        RealmResource realmResource = keycloakAdmin.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Check if username already exists
        List<UserRepresentation> existingUsername = usersResource.search(request.getUsername(), true);
        if (!existingUsername.isEmpty()) {
            throw new ResourceAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        List<UserRepresentation> existingEmail = usersResource.searchByEmail(request.getEmail(), true);
        if (!existingEmail.isEmpty()) {
            throw new ResourceAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Create user representation with ONLY auth fields
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true); // Will be email verified automatically


        // Create user in Keycloak
        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                String userId = getCreatedId(response);
                log.info("✅ User created in Keycloak with ID: {}", userId);

                // Set password
                setUserPassword(usersResource, userId, request.getPassword());

                // Assign default role (ROLE_GUEST)
                assignDefaultRole(realmResource, userId);

                // Publish UserRegisteredEvent for other services
                publishUserRegisteredEvent(userId, request);


                return getUserById(userId);
            } else {
                log.error("❌ Failed to create user. Status: {}", response.getStatus());
                throw new KeycloakException("Failed to create user. Status: " + response.getStatus());
            }
        } catch (ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error creating user: {}", e.getMessage());
            throw new KeycloakException("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Publish UserRegisteredEvent to Kafka
     */
    private void publishUserRegisteredEvent(String userId, RegisterRequest request) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .timestamp(System.currentTimeMillis())
                .keycloakUserId(userId)
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .countryCode(request.getCountryCode())
                .contactNo(request.getContactNo())
                .organization(request.getOrganization())
                .build();

        eventPublisher.publishUserRegisteredEvent(event);
        log.info("📤 UserRegisteredEvent published for user: {}", request.getUsername());
    }

    /**
     * Get user by ID (simplified - basic auth info only)
     */
    public UserResponse getUserById(String userId) {
        log.debug("Fetching user by ID: {}", userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            // Get user roles
            List<String> roles = userResource.roles().realmLevel().listEffective().stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());

            return mapToUserResponse(user, roles);

        } catch (Exception e) {
            log.error("❌ Error fetching user: {}", e.getMessage());
            throw new ResourceNotFoundException("User not found: " + userId);
        }
    }

    /**
     * Get all users (Admin only)
     */
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.list();

            if (users.isEmpty()) {
                throw new ResourceNotFoundException("No users found in the system");
            }

            return users.stream()
                    .map(user -> {
                        List<String> roles = realmResource.users().get(user.getId())
                                .roles().realmLevel().listEffective().stream()
                                .map(RoleRepresentation::getName)
                                .collect(Collectors.toList());
                        return mapToUserResponse(user, roles);
                    })
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error fetching users: {}", e.getMessage());
            throw new KeycloakException("Error fetching users: " + e.getMessage());
        }
    }

    /**
     * Delete user (soft delete - disable user)
     */
    public void deleteUser(String userId) {
        log.warn("Soft deleting user: {}", userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            // Disable user (soft delete)
            user.setEnabled(false);
            userResource.update(user);

            log.info("🟡 User soft deleted (disabled): {}", userId);

            // Publish UserDeletedEvent
            publishUserDeletedEvent(userId, user.getUsername());

        } catch (Exception e) {
            log.error("❌ Error soft deleting user: {}", e.getMessage());
            throw new KeycloakException("Error soft deleting user: " + e.getMessage());
        }
    }

    /**
     * Publish UserDeletedEvent to Kafka
     */
    private void publishUserDeletedEvent(String userId, String username) {
        String deletedBy = getCurrentUsername();

        UserDeletedEvent event = UserDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_DELETED")
                .timestamp(System.currentTimeMillis())
                .keycloakUserId(userId)
                .username(username)
                .deletedBy(deletedBy)
                .build();

        eventPublisher.publishUserDeletedEvent(event);
        log.info("📤 UserDeletedEvent published for user: {}", username);
    }

    /**
     * Assign role to user
     */
    public UserResponse assignRoleToUser(String userId, String roleName) {
        log.info("Assigning role '{}' to user: {}", roleName, userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            // Check if user already has the role
            List<RoleRepresentation> existingRoles = userResource.roles().realmLevel().listEffective();
            boolean hasRole = existingRoles.stream()
                    .anyMatch(r -> r.getName().equalsIgnoreCase(roleName));

            if (hasRole) {
                throw new KeycloakException("User already has role: " + roleName);
            }

            // Get role
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // Assign role
            userResource.roles().realmLevel().add(Collections.singletonList(role));
            log.info("✅ Role '{}' assigned to user: {}", roleName, userId);

            // Publish UserRoleChangedEvent
            publishUserRoleChangedEvent(userId, user.getUsername(), roleName, "ASSIGNED");

            return getUserById(userId);

        } catch (KeycloakException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error assigning role: {}", e.getMessage());
            throw new KeycloakException("Error assigning role: " + e.getMessage());
        }
    }

    /**
     * Remove role from user
     */
    public UserResponse removeRoleFromUser(String userId, String roleName) {
        log.info("Removing role '{}' from user: {}", roleName, userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            // Get role
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // Remove role
            userResource.roles().realmLevel().remove(Collections.singletonList(role));
            log.info("✅ Role '{}' removed from user: {}", roleName, userId);

            // Publish UserRoleChangedEvent
            publishUserRoleChangedEvent(userId, user.getUsername(), roleName, "REMOVED");

            return getUserById(userId);

        } catch (Exception e) {
            log.error("❌ Error removing role: {}", e.getMessage());
            throw new KeycloakException("Error removing role: " + e.getMessage());
        }
    }

    /**
     * Publish UserRoleChangedEvent to Kafka
     */
    private void publishUserRoleChangedEvent(String userId, String username, String roleName, String action) {
        String changedBy = getCurrentUsername();

        UserRoleChangedEvent event = UserRoleChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_ROLE_CHANGED")
                .timestamp(System.currentTimeMillis())
                .keycloakUserId(userId)
                .username(username)
                .roleName(roleName)
                .action(action)
                .changedBy(changedBy)
                .build();

        eventPublisher.publishUserRoleChangedEvent(event);
        log.info("📤 UserRoleChangedEvent published for user: {} (role: {}, action: {})", username, roleName, action);
    }

    // ============================================
    // Helper Methods
    // ============================================

    private void setUserPassword(UsersResource usersResource, String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        usersResource.get(userId).resetPassword(credential);
        log.debug("Password set for user: {}", userId);
    }

    private void assignDefaultRole(RealmResource realmResource, String userId) {
        try {
            RoleRepresentation guestRole = realmResource.roles().get("ROLE_GUEST").toRepresentation();
            realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(guestRole));
            log.debug("Default role 'ROLE_GUEST' assigned to user: {}", userId);
        } catch (Exception e) {
            log.warn("⚠️ Failed to assign default role: {}", e.getMessage());
        }
    }

    private String getCreatedId(Response response) {
        String location = response.getHeaderString("Location");
        if (location != null) {
            return location.substring(location.lastIndexOf('/') + 1);
        }
        throw new KeycloakException("Failed to extract user ID from response");
    }

    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Could not get current username: {}", e.getMessage());
        }
        return "SYSTEM";
    }

    /**
     * Map to simplified UserResponse (NO extended profile fields)
     */
    private UserResponse mapToUserResponse(UserRepresentation user, List<String> roles) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdTimestamp(user.getCreatedTimestamp())
                .roles(roles.stream().collect(Collectors.toSet()))
                .build();
    }
}