package com.collabrix.auth.service;

import com.collabrix.auth.dto.RoleResponse;
import com.collabrix.auth.entity.Role;
import com.collabrix.auth.repository.RoleRepository;
import com.collabrix.common.libraries.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Create a new role in the system.
     */
    @Transactional
    public RoleResponse createRole(String roleName, boolean systemDefined) {
        if (roleRepository.existsByName(roleName)) {
            log.warn("Role creation failed — role '{}' already exists", roleName);
            throw new ResourceAlreadyExistsException("Role already exists: " + roleName);
        }

        Role role = Role.builder()
                .name(roleName.toUpperCase()) // normalize role names
                .systemDefined(systemDefined)
                .build();

        Role saved = roleRepository.save(role);
        log.info("Created new role: {}", saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public void softDeleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (role.isSystemDefined())
            throw new BusinessRuleViolationException("Cannot soft delete system-defined role: " + role.getName());

        role.setDeleted(true);
        roleRepository.save(role);
        log.info("Soft deleted role: {}", role.getName());
    }

    @Transactional
    public void hardDeleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (role.isSystemDefined())
            throw new BusinessRuleViolationException("Cannot hard delete system-defined role: " + role.getName());

        roleRepository.delete(role);
        log.warn("Hard deleted role permanently: {}", role.getName());
    }


    /**
     * Fetch all roles (non - deleted).
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll().stream().filter(u -> !u.isDeleted()).map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Fetch a role by name.
     */
    @Transactional(readOnly = true)
    public void getRoleByName(String name) {
        toResponse(roleRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name)));
    }

    public RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .systemDefined(role.isSystemDefined())
                .build();
    }
}
