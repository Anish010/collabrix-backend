package com.collabrix.auth.controller;

import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User management endpoints (protected by Keycloak JWT).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final KeycloakUserService keycloakUserService;

    /**
     * Get all users (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("📋 Fetching all users");
        List<UserResponse> users = keycloakUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        log.info("👤 Fetching user: {}", userId);
        UserResponse user = keycloakUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @RequestBody UserRepresentation userUpdate) {
        log.info("✏️ Updating user: {}", userId);
        UserResponse user = keycloakUserService.updateUser(userId, userUpdate);
        return ResponseEntity.ok(user);
    }

    /**
     * Delete user (Admin only)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        log.info("🗑️ Deleting user: {}", userId);
        keycloakUserService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
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
}