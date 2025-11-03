package com.collabrix.auth.service;

import com.collabrix.auth.dto.RoleResponse;
import com.collabrix.common.libraries.exceptions.KeycloakException;
import com.collabrix.common.libraries.exceptions.ResourceAlreadyExistsException;
import com.collabrix.common.libraries.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Keycloak administrative operations (roles, realm management).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Create a new realm role
     */
    public RoleResponse createRole(String roleName, String description) {
        log.info("Creating role: {}", roleName);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            RolesResource rolesResource = realmResource.roles();

            // Check if role already exists
            try {
                rolesResource.get(roleName).toRepresentation();
                throw new ResourceAlreadyExistsException("Role already exists: " + roleName);
            } catch (jakarta.ws.rs.NotFoundException e) {
                // Role doesn't exist, proceed with creation
            }

            // Create role
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            role.setDescription(description);

            rolesResource.create(role);
            log.info("✅ Role created: {}", roleName);

            return getRoleByName(roleName);

        } catch (ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error creating role: {}", e.getMessage());
            throw new KeycloakException("Error creating role: " + e.getMessage());
        }
    }

    /**
     * Get role by name
     */
    public RoleResponse getRoleByName(String roleName) {
        log.debug("Fetching role: {}", roleName);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            return mapToRoleResponse(role);

        } catch (Exception e) {
            log.error("❌ Error fetching role: {}", e.getMessage());
            throw new ResourceNotFoundException("Role not found: " + roleName);
        }
    }

    /**
     * Get all realm roles
     */
    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            List<RoleRepresentation> roles = realmResource.roles().list();

            return roles.stream()
                    .map(this::mapToRoleResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error fetching roles: {}", e.getMessage());
            throw new KeycloakException("Error fetching roles: " + e.getMessage());
        }
    }

    /**
     * Update role
     */
    public RoleResponse updateRole(String roleName, String newDescription) {
        log.info("Updating role: {}", roleName);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            role.setDescription(newDescription);

            realmResource.roles().get(roleName).update(role);
            log.info("✅ Role updated: {}", roleName);

            return getRoleByName(roleName);

        } catch (Exception e) {
            log.error("❌ Error updating role: {}", e.getMessage());
            throw new KeycloakException("Error updating role: " + e.getMessage());
        }
    }

    /**
     * Delete role
     */
    public void deleteRole(String roleName) {
        log.warn("Deleting role: {}", roleName);

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            realmResource.roles().deleteRole(roleName);
            log.info("✅ Role deleted: {}", roleName);

        } catch (Exception e) {
            log.error("❌ Error deleting role: {}", e.getMessage());
            throw new KeycloakException("Error deleting role: " + e.getMessage());
        }
    }

    // ============================================
    // Helper Methods
    // ============================================

    private RoleResponse mapToRoleResponse(RoleRepresentation role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .composite(role.isComposite())
                .clientRole(role.getClientRole())
                .build();
    }
}