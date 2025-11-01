package com.collabrix.auth.config;

import com.collabrix.auth.dto.RoleResponse;
import com.collabrix.auth.service.RoleService;
import com.collabrix.common.libraries.exceptions.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleService roleService;

    /**
     * Seed essential system-defined roles on startup.
     */
    @PostConstruct
    public void seedRoles() {
        log.info("🔁 Checking and seeding system-defined roles...");

        List<String> rolesToSeed = List.of("ROLE_GUEST", "ROLE_ADMIN");

        rolesToSeed.forEach(roleName -> {
            try {
                roleService.getRoleByName(roleName);
                log.debug("✅ Role '{}' already exists.", roleName);
            } catch (ResourceNotFoundException e) {
                RoleResponse role = roleService.createRole(roleName, true);
                log.info("🆕 Created missing system role '{}'", role.getName());
            } catch (Exception e) {
                log.error("❌ Unexpected error while verifying role '{}': {}", roleName, e.getMessage());
            }
        });

        log.info("✅ Role seeding complete.");
    }

}
