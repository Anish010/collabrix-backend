package com.collabrix.auth.service;

import com.collabrix.auth.dto.UserRegisterRequest;
import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.service.interfaces.KeycloakUserService;
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
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    // ======================================================
    // CREATE KEYCLOAK USER ONLY  (used by RegistrationOrchestrator)
    // ======================================================
    @Override
    @Transactional
    public UserResponse createUserOnly(UserRegisterRequest request) {
        log.info("🔐 Creating Keycloak user only for {}", request.getUsername());

        RealmResource realmResource = keycloakAdmin.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Prevent duplicates
        if (!usersResource.search(request.getUsername(), true).isEmpty()) {
            throw new ResourceAlreadyExistsException("Username already exists: " + request.getUsername());
        }
        if (!usersResource.searchByEmail(request.getEmail(), true).isEmpty()) {
            throw new ResourceAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Create base user
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);

        try (Response response = usersResource.create(user)) {
            if (response.getStatus() != 201) {
                throw new KeycloakException("Failed to create user in Keycloak");
            }

            String userId = extractUserId(response);
            log.info("✔️ Keycloak user created: {}", userId);

            setUserPassword(usersResource, userId, request.getPassword());
            assignRole(realmResource, userId, "ROLE_GUEST");

            return getUserById(userId);

        } catch (Exception e) {
            log.error("❌ Failed to create Keycloak user: {}", e.getMessage());
            throw new KeycloakException("Keycloak user creation failed: " + e.getMessage());
        }
    }

    // ======================================================
    // GET USER BASIC DETAILS
    // ======================================================
    @Override
    public UserResponse getUserById(String userId) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserRepresentation user = realmResource.users().get(userId).toRepresentation();

            List<String> roles = realmResource.users().get(userId)
                    .roles().realmLevel().listEffective()
                    .stream().map(RoleRepresentation::getName)
                    .collect(Collectors.toList());

            return mapToUserResponse(user, roles);

        } catch (Exception e) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
    }

    // ======================================================
    // SOFT DELETE USER
    // ======================================================
    @Override
    @Transactional
    public void deleteUser(String userId) {
        log.warn("🟡 Soft deleting Keycloak user {}", userId);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(false);
            userResource.update(user);

        } catch (Exception e) {
            throw new KeycloakException("Soft delete failed: " + e.getMessage());
        }
    }

    // ======================================================
    // HARD DELETE USER
    // ======================================================
    @Override
    @Transactional
    public void hardDeleteUser(String userId) {
        log.error("❌ Permanently deleting Keycloak user {}", userId);

        try {
            keycloakAdmin.realm(realm).users().get(userId).remove();
        } catch (Exception e) {
            throw new KeycloakException("Hard delete failed: " + e.getMessage());
        }
    }

    // ======================================================
    // ROLE MANAGEMENT
    // ======================================================
    @Override
    @Transactional
    public UserResponse assignRoleToUser(String userId, String roleName) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        assignRole(realmResource, userId, roleName);
        return getUserById(userId);
    }

    @Override
    @Transactional
    public UserResponse removeRoleFromUser(String userId, String roleName) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        realmResource.users().get(userId)
                .roles().realmLevel()
                .remove(Collections.singletonList(
                        realmResource.roles().get(roleName).toRepresentation()
                ));
        return getUserById(userId);
    }

    // ======================================================
    // INTERNAL HELPERS
    // ======================================================
    private String extractUserId(Response response) {
        String location = response.getHeaderString("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private void assignRole(RealmResource realmResource, String userId, String roleName) {
        try {
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            realmResource.users().get(userId)
                    .roles().realmLevel().add(Collections.singletonList(role));
        } catch (Exception e) {
            log.warn("⚠ Role {} not assigned to {}: {}", roleName, userId, e.getMessage());
        }
    }

    private void setUserPassword(UsersResource usersResource, String userId, String password) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);
        usersResource.get(userId).resetPassword(cred);
    }

    private UserResponse mapToUserResponse(UserRepresentation user, List<String> roles) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .roles(new HashSet<>(roles))
                .createdTimestamp(user.getCreatedTimestamp())
                .build();
    }
}