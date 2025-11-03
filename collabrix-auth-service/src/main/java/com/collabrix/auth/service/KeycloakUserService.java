package com.collabrix.auth.service;

import com.collabrix.auth.dto.RegisterRequest;
import com.collabrix.auth.dto.UserResponse;
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
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Keycloak user management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Register a new user in Keycloak
     */
    public UserResponse registerUser(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

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

        // Create user representation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        // Add custom attributes (countryCode, contactNo, organization)
        Map<String, List<String>> attributes = new HashMap<>();
        if (request.getCountryCode() != null) {
            attributes.put("countryCode", Collections.singletonList(request.getCountryCode()));
        }
        if (request.getContactNo() != null) {
            attributes.put("contactNo", Collections.singletonList(request.getContactNo()));
        }
        if (request.getOrganization() != null) {
            attributes.put("organization", Collections.singletonList(request.getOrganization()));
        }
        user.setAttributes(attributes);

        // Create user in Keycloak
        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                String userId = getCreatedId(response);
                log.info("✅ User created with ID: {}", userId);

                // Set password
                setUserPassword(usersResource, userId, request.getPassword());

                // Assign default role (ROLE_GUEST)
                assignDefaultRole(realmResource, userId);

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
     * Get user by ID
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
     * Get all users
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
     * Update user
     */
    public UserResponse updateUser(String userId, UserRepresentation userUpdate) {
        log.info("Updating user: {}", userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation existingUser = userResource.toRepresentation();

            // Check if user is active
            if (!existingUser.isEnabled()) {
                throw new KeycloakException("Cannot update an inactive user");
            }

            // Update basic fields
            if (userUpdate.getFirstName() != null) {
                existingUser.setFirstName(userUpdate.getFirstName());
            }
            if (userUpdate.getLastName() != null) {
                existingUser.setLastName(userUpdate.getLastName());
            }
            if (userUpdate.getEmail() != null) {
                existingUser.setEmail(userUpdate.getEmail());
            }

            // Update custom attributes
            Map<String, List<String>> attributes = existingUser.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }

            if (userUpdate.getAttributes() != null) {
                // Update countryCode
                if (userUpdate.getAttributes().containsKey("countryCode")) {
                    attributes.put("countryCode", userUpdate.getAttributes().get("countryCode"));
                }
                // Update contactNo
                if (userUpdate.getAttributes().containsKey("contactNo")) {
                    attributes.put("contactNo", userUpdate.getAttributes().get("contactNo"));
                }
                // Update organization
                if (userUpdate.getAttributes().containsKey("organization")) {
                    attributes.put("organization", userUpdate.getAttributes().get("organization"));
                }
            }

            existingUser.setAttributes(attributes);
            userResource.update(existingUser);
            log.info("✅ User updated successfully: {}", userId);

            return getUserById(userId);

        } catch (Exception e) {
            log.error("❌ Error updating user: {}", e.getMessage());
            throw new KeycloakException("Error updating user: " + e.getMessage());
        }
    }

    /**
     * Soft delete user (disable user)
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

            log.info("🟠 User soft deleted (disabled): {}", userId);

        } catch (Exception e) {
            log.error("❌ Error soft deleting user: {}", e.getMessage());
            throw new KeycloakException("Error soft deleting user: " + e.getMessage());
        }
    }

    /**
     * Hard delete user (permanently delete from Keycloak)
     */
    public void hardDeleteUser(String userId) {
        log.error("Hard deleting user: {}", userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            realmResource.users().delete(userId);
            log.warn("⚠️ User permanently deleted: {}", userId);

        } catch (Exception e) {
            log.error("❌ Error hard deleting user: {}", e.getMessage());
            throw new KeycloakException("Error hard deleting user: " + e.getMessage());
        }
    }

    /**
     * Assign role to user
     */
    public UserResponse assignRoleToUser(String userId, String roleName) {
        log.info("Assigning role '{}' to user: {}", roleName, userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

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

            // Get role
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // Remove role
            userResource.roles().realmLevel().remove(Collections.singletonList(role));
            log.info("✅ Role '{}' removed from user: {}", roleName, userId);

            return getUserById(userId);

        } catch (Exception e) {
            log.error("❌ Error removing role: {}", e.getMessage());
            throw new KeycloakException("Error removing role: " + e.getMessage());
        }
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

    private UserResponse mapToUserResponse(UserRepresentation user, List<String> roles) {
        // Extract custom attributes
        Map<String, List<String>> attributes = user.getAttributes();
        String countryCode = null;
        String contactNo = null;
        String organization = null;

        if (attributes != null) {
            countryCode = attributes.containsKey("countryCode")
                    ? attributes.get("countryCode").get(0)
                    : null;
            contactNo = attributes.containsKey("contactNo")
                    ? attributes.get("contactNo").get(0)
                    : null;
            organization = attributes.containsKey("organization")
                    ? attributes.get("organization").get(0)
                    : null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .countryCode(countryCode)
                .contactNo(contactNo)
                .organization(organization)
                .active(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdTimestamp(user.getCreatedTimestamp())
                .roles(roles.stream().collect(Collectors.toSet()))
                .build();
    }
}