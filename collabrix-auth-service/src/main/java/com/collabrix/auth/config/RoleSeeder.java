package com.collabrix.auth.config;

import com.collabrix.auth.service.KeycloakRoleService;
import com.collabrix.common.libraries.exceptions.ResourceAlreadyExistsException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds default roles in Keycloak on application startup.
 * Updated to use renamed KeycloakRoleService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final KeycloakRoleService keycloakRoleService;

    @PostConstruct
    public void seedRoles() {
        log.info("🌱 Seeding default roles in Keycloak...");

        List<String> defaultRoles = List.of("ROLE_GUEST", "ROLE_ADMIN", "ROLE_MANAGER");

        for (String roleName : defaultRoles) {
            try {
                keycloakRoleService.createRole(roleName, "Default " + roleName);
                log.info("✅ Created role: {}", roleName);
            } catch (ResourceAlreadyExistsException e) {
                log.debug("Role already exists: {}", roleName);
            } catch (Exception e) {
                log.error("❌ Failed to create role {}: {}", roleName, e.getMessage());
            }
        }

        log.info("✅ Role seeding complete");
    }
}