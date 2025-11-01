package com.collabrix.auth.controller;

import com.collabrix.auth.dto.RoleRequest;
import com.collabrix.auth.dto.RoleResponse;
import com.collabrix.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Create a new role (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> createRole(@RequestBody RoleRequest request) {
        RoleResponse role = roleService.createRole(request.getName(), false);
        log.info("Role created via API: {}", role.getName());
        return ResponseEntity.ok(role);
    }

    /**
     * Soft delete a role (marks deleted=true)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> softDeleteRole(@PathVariable Long id) {
        roleService.softDeleteRole(id);
        log.info("Role soft deleted via API: id={}", id);
        return ResponseEntity.ok("Role soft-deleted successfully");
    }

    /**
     * Hard delete a role (permanent removal)
     */
    @DeleteMapping("/hard/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> hardDeleteRole(@PathVariable Long id) {
        roleService.hardDeleteRole(id);
        log.warn("⚠️ Role permanently deleted via API: id={}", id);
        return ResponseEntity.ok("Role permanently deleted");
    }

    /**
     * Get all roles — public access (e.g., to display available roles)
     */
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

}
