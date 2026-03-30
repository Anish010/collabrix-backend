package com.collabrix.user.controller;

import com.collabrix.common.libraries.dto.ApiResponse;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.dto.UserStatisticsResponse;
import com.collabrix.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user profile ONLY ADMIN operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Soft delete user profile (Admin only)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable String userId) {
        log.info("📥 ADMIN DELETE user {}", userId);
        userProfileService.deleteProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("User profile deleted", null));
    }

    /**
     * Unsuspend user profile (Admin only)
     */
    @PostMapping("/{userId}/reactivate")
    public ResponseEntity<ApiResponse<UserProfileResponse>> reactivate(@PathVariable String userId) {
        log.info("📥 ADMIN REACTIVATE user {}", userId);
        UserProfileResponse response = userProfileService.activateProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 📋 All users
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        log.info("📥 ADMIN GET all active users");
        return ResponseEntity.ok(ApiResponse.success(userProfileService.getAllUsers()));
    }

    /**
     * 📋 Users missing required profile info
     */
    @GetMapping("/incomplete")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getIncomplete() {
        log.info("📥 ADMIN GET incomplete profiles");
        return ResponseEntity.ok(ApiResponse.success(userProfileService.getUsersWithIncompleteProfiles()));
    }

    /**
     * 📊 User statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> stats() {
        log.info("📥 ADMIN GET statistics");
        return ResponseEntity.ok(ApiResponse.success(userProfileService.getUserStatistics()));
    }

    /**
     * 🕒 Override last login timestamp
     * (used rarely, mostly for data correction)
     */
    @PostMapping("/{userId}/last-login")
    public ResponseEntity<ApiResponse<Void>> updateLastLogin(@PathVariable String userId) {
        log.info("📥 ADMIN UPDATE last login for {}", userId);
        userProfileService.updateLastLogin(userId);
        return ResponseEntity.ok(ApiResponse.success("Last login updated", null));
    }

    /**
     * 🏢 Fetch users by org
     */
    @GetMapping("/organization/{org}")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> byOrg(@PathVariable("org") String org) {
        log.info("📥 ADMIN GET users by org {}", org);
        return ResponseEntity.ok(ApiResponse.success(userProfileService.getUsersByOrganization(org)));
    }
}