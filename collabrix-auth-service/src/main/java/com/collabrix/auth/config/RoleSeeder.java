package com.collabrix.auth.config;

import com.collabrix.auth.entity.Role;
import com.collabrix.auth.service.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleService roleService;

    /**
     * Seed system-defined roles at application startup.
     */
    @PostConstruct
    public void seedRoles() {
        log.info("Seeding system-defined roles...");

        createRoleIfMissing("GUEST");
        createRoleIfMissing("ADMIN");

        log.info("System roles verified or created.");
    }

    private void createRoleIfMissing(String roleName) {
        try {
            roleService.getRoleByName(roleName);
            log.info("Role '{}' already exists", roleName);
        } catch (Exception e) {
            // If not found, create it as systemDefined
            Role role = roleService.createRole(roleName, true);
            log.info("Created missing system role '{}'", role.getName());
        }
    }
}
