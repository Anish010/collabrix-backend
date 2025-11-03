package com.collabrix.auth.controller;

import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User-Role assignment endpoints (Admin only).
 * User profile management is moved to user-service.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final KeycloakUserService keycloakUserService;

    /**
     * Get user by ID (basic auth info only)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        log.info("👤 Fetching user: {}", userId);
        UserResponse user = keycloakUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Assign role to user (Admin only)
     */
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        log.info("🎭 Assigning role to user: {}", userId);
        String roleName = request.get("roleName");
        UserResponse user = keycloakUserService.assignRoleToUser(userId, roleName);
        return ResponseEntity.ok(user);
    }

    /**
     * Remove role from user (Admin only)
     */
    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable String userId,
            @PathVariable String roleName) {
        log.info("🎭 Removing role from user: {}", userId);
        UserResponse user = keycloakUserService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user's roles
     */
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<UserResponse> getUserRoles(@PathVariable String userId) {
        log.info("🎭 Fetching roles for user: {}", userId);
        UserResponse user = keycloakUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Soft delete user (Admin only)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        log.info("🗑️ Soft deleting user: {}", userId);
        keycloakUserService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User soft deleted successfully"));
    }
}