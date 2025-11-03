package com.collabrix.auth.controller;

import com.collabrix.auth.dto.RoleResponse;
import com.collabrix.auth.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Role management endpoints (Admin only).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final KeycloakAdminService keycloakAdminService;

    /**
     * Get all roles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        log.info("📋 Fetching all roles");
        List<RoleResponse> roles = keycloakAdminService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Get role by name
     */
    @GetMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> getRoleByName(@PathVariable String roleName) {
        log.info("🎭 Fetching role: {}", roleName);
        RoleResponse role = keycloakAdminService.getRoleByName(roleName);
        return ResponseEntity.ok(role);
    }

    /**
     * Create a new role
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> createRole(@RequestBody Map<String, String> request) {
        log.info("➕ Creating role: {}", request.get("name"));
        String roleName = request.get("name");
        String description = request.get("description");
        RoleResponse role = keycloakAdminService.createRole(roleName, description);
        return ResponseEntity.ok(role);
    }

    /**
     * Update role
     */
    @PutMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable String roleName,
            @RequestBody Map<String, String> request) {
        log.info("✏️ Updating role: {}", roleName);
        String newDescription = request.get("description");
        RoleResponse role = keycloakAdminService.updateRole(roleName, newDescription);
        return ResponseEntity.ok(role);
    }

    /**
     * Delete role
     */
    @DeleteMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteRole(@PathVariable String roleName) {
        log.info("🗑️ Deleting role: {}", roleName);
        keycloakAdminService.deleteRole(roleName);
        return ResponseEntity.ok(Map.of("message", "Role deleted successfully"));
    }
}