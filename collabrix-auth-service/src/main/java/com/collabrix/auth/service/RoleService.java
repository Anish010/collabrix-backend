package com.collabrix.auth.service;

import com.collabrix.auth.entity.Role;
import com.collabrix.auth.repository.RoleRepository;
import com.collabrix.common.libraries.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    // Create a role
    public Role createRole(String roleName, boolean systemDefined) {
        if (roleRepository.existsByName(roleName)) {
            log.warn("Role {} already exists", roleName);
            throw new ResourceAlreadyExistsException("Role already exists: " + roleName);
        }
        Role role = Role.builder()
                .name(roleName)
                .systemDefined(systemDefined)
                .build();
        log.info("Creating new role: {}", roleName);
        return roleRepository.save(role);
    }

    // Delete a role
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (role.isSystemDefined()) {
            log.error("Cannot delete system-defined role: {}", role.getName());
            throw new BusinessRuleViolationException("Cannot delete system-defined role: " + role.getName());
        }

        roleRepository.delete(role);
        log.info("Deleted role: {}", role.getName());
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
    }
}
