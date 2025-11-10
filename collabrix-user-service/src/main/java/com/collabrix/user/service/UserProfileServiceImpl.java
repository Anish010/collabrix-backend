package com.collabrix.user.service;

import com.collabrix.user.dto.UpdateProfileRequest;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.dto.UserStatisticsResponse;
import com.collabrix.user.entity.UserProfile;
import com.collabrix.user.exception.InactiveUserException;
import com.collabrix.user.exception.UserNotFoundException;
import com.collabrix.user.kafka.events.UserRegisteredEvent;
import com.collabrix.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserProfileService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfileResponse createProfile(UserRegisteredEvent event) {
        log.info("Creating profile for user: {} ({})", event.getUsername(), event.getKeycloakUserId());

        // Idempotency check
        if (userProfileRepository.existsById(event.getKeycloakUserId())) {
            log.warn("⚠️ Profile already exists for user: {}", event.getKeycloakUserId());
            return getProfileById(event.getKeycloakUserId());
        }

        // Create new profile
        UserProfile profile = UserProfile.builder()
                .id(event.getKeycloakUserId())
                .username(event.getUsername())
                .email(event.getEmail())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .countryCode(event.getCountryCode())
                .contactNo(event.getContactNo())
                .organization(event.getOrganization())
                .roles(event.getRoles() != null ? event.getRoles() : new ArrayList<>())
                .active(true)
                .profileCompleted(false)
                .build();

        // Calculate initial profile completion
        profile.calculateProfileCompletion();

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("✅ Profile created successfully for user: {}", savedProfile.getUsername());

        return mapToResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileById(String userId) {
        log.debug("Fetching profile by ID: {}", userId);
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String username) {
        log.debug("Fetching profile by username: {}", username);
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return mapToResponse(profile);
    }

    @Override
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // Check if user is active
        if (!profile.getActive()) {
            throw new InactiveUserException("Cannot update inactive user profile");
        }

        // Update fields (only if provided)
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getCountryCode() != null) {
            profile.setCountryCode(request.getCountryCode());
        }
        if (request.getContactNo() != null) {
            profile.setContactNo(request.getContactNo());
        }
        if (request.getOrganization() != null) {
            profile.setOrganization(request.getOrganization());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getGithubUrl() != null) {
            profile.setGithubUrl(request.getGithubUrl());
        }
        if (request.getTwitterUrl() != null) {
            profile.setTwitterUrl(request.getTwitterUrl());
        }
        if (request.getWebsiteUrl() != null) {
            profile.setWebsiteUrl(request.getWebsiteUrl());
        }

        // Recalculate profile completion
        profile.calculateProfileCompletion();

        UserProfile updatedProfile = userProfileRepository.save(profile);
        log.info("✅ Profile updated successfully for user: {}", updatedProfile.getUsername());

        return mapToResponse(updatedProfile);
    }

    @Override
    public UserProfileResponse updateAvatar(String userId, String avatarUrl) {
        log.info("Updating avatar for user: {}", userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (!profile.getActive()) {
            throw new InactiveUserException("Cannot update avatar for inactive user");
        }

        profile.setAvatarUrl(avatarUrl);
        profile.calculateProfileCompletion();

        UserProfile updatedProfile = userProfileRepository.save(profile);
        log.info("✅ Avatar updated successfully for user: {}", updatedProfile.getUsername());

        return mapToResponse(updatedProfile);
    }

    @Override
    public void deleteProfile(String userId) {
        log.warn("Soft deleting profile for user: {}", userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        profile.deactivate();
        userProfileRepository.save(profile);

        log.info("🟠 Profile soft deleted for user: {}", profile.getUsername());
    }

    @Override
    public void hardDeleteProfile(String userId) {
        log.error("Hard deleting profile for user: {}", userId);

        if (!userProfileRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }

        userProfileRepository.deleteById(userId);
        log.warn("⚠️ Profile permanently deleted for user: {}", userId);
    }

    @Override
    public UserProfileResponse reactivateProfile(String userId) {
        log.info("Reactivating profile for user: {}", userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        profile.activate();
        UserProfile reactivatedProfile = userProfileRepository.save(profile);

        log.info("✅ Profile reactivated for user: {}", reactivatedProfile.getUsername());
        return mapToResponse(reactivatedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> searchUsers(String searchTerm) {
        log.debug("Searching users with term: {}", searchTerm);
        List<UserProfile> profiles = userProfileRepository.searchUsers(searchTerm);
        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllActiveUsers() {
        log.debug("Fetching all active users");
        List<UserProfile> profiles = userProfileRepository.findByActiveTrue();
        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersByOrganization(String organization) {
        log.debug("Fetching users by organization: {}", organization);
        List<UserProfile> profiles = userProfileRepository.findByOrganization(organization);
        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersWithIncompleteProfiles() {
        log.debug("Fetching users with incomplete profiles");
        List<UserProfile> profiles = userProfileRepository.findUsersWithIncompleteProfiles();
        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        log.debug("Calculating user statistics");

        long totalUsers = userProfileRepository.count();
        long activeUsers = userProfileRepository.countByActiveTrue();
        long inactiveUsers = totalUsers - activeUsers;
        long completedProfiles = userProfileRepository.countByProfileCompletedTrue();

        List<UserProfile> allProfiles = userProfileRepository.findAll();
        double avgCompletion = allProfiles.stream()
                .mapToInt(UserProfile::getProfileCompletionPercentage)
                .average()
                .orElse(0.0);

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .completedProfiles(completedProfiles)
                .averageProfileCompletion(Math.round(avgCompletion))
                .build();
    }

    @Override
    public void updateLastLogin(String userId) {
        log.debug("Updating last login for user: {}", userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        profile.updateLastLogin();
        userProfileRepository.save(profile);
    }

    @Override
    public UserProfileResponse addRole(String userId, String role) {
        log.info("Adding role '{}' to user {}", role, userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (!profile.getRoles().contains(role)) {
            profile.getRoles().add(role.toUpperCase());
            userProfileRepository.save(profile);
            log.info("✅ Role '{}' added to user {}", role, userId);
        } else {
            log.info("ℹ️ User {} already has role '{}'", userId, role);
        }

        return mapToResponse(profile);
    }

    @Override
    public UserProfileResponse removeRole(String userId, String role) {
        log.info("Removing role '{}' from user {}", role, userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (profile.getRoles().removeIf(r -> r.equalsIgnoreCase(role))) {
            userProfileRepository.save(profile);
            log.info("✅ Role '{}' removed from user {}", role, userId);
        } else {
            log.info("ℹ️ User {} did not have role '{}'", userId, role);
        }

        return mapToResponse(profile);
    }

    // Helper method to map entity to response DTO
    private UserProfileResponse mapToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .countryCode(profile.getCountryCode())
                .contactNo(profile.getContactNo())
                .organization(profile.getOrganization())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .twitterUrl(profile.getTwitterUrl())
                .websiteUrl(profile.getWebsiteUrl())
                .roles(profile.getRoles())
                .active(profile.getActive())
                .profileCompleted(profile.getProfileCompleted())
                .profileCompletionPercentage(profile.getProfileCompletionPercentage())
                .lastLoginAt(profile.getLastLoginAt())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

}