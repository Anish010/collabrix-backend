package com.collabrix.user.service;

import com.collabrix.common.libraries.dto.UserProfileCreateRequest;
import com.collabrix.common.libraries.events.UserLoginEvent;
import com.collabrix.common.libraries.events.UserLogoutEvent;
import com.collabrix.common.libraries.exceptions.BusinessRuleViolationException;
import com.collabrix.user.dto.UpdateProfileRequest;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.dto.UserStatisticsResponse;
import com.collabrix.user.entity.UserProfile;
import com.collabrix.user.entity.UserRole;
import com.collabrix.user.exception.InactiveUserException;
import com.collabrix.user.exception.UserNotFoundException;
import com.collabrix.user.mapper.UserProfileMapper;
import com.collabrix.user.repository.UserProfileRepository;
import com.collabrix.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final UserProfileMapper userProfileMapper;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleService userRoleService;

    @Override
    public UserProfileResponse createProfile(UserProfileCreateRequest req) {
        log.info("Creating profile for user: {} ({})", req.getUsername(), req.getKeycloakUserId());

        String userId = req.getKeycloakUserId();

        // Idempotency — return existing profile if already created
        if (userProfileRepository.existsById(userId)) {
            log.warn("⚠️ Profile already exists for user: {}", userId);
            return getProfileById(userId);
        }

        if(userProfileRepository.existsByUsername(req.getUsername())) {
            throw new BusinessRuleViolationException("Username already taken: " + req.getUsername());
        }

        if(userProfileRepository.existsByEmail(req.getEmail())) {
            throw new BusinessRuleViolationException("Email already registered: " + req.getEmail());
        }

        if(req.getContactNo() != null && userProfileRepository.existsByContactNo(req.getContactNo())) {
            throw new BusinessRuleViolationException("Contact number already registered: " + req.getContactNo());
        }

        // Create new profile
        UserProfile profile = UserProfile.builder()
                .id(userId)
                .username(req.getUsername())
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .countryCode(req.getCountryCode())
                .contactNo(req.getContactNo())
                .organization(req.getOrganization())
                .active(true)
                .deleted(false)
                .loginCount(0)
                .logoutCount(0)
                .profileCompleted(false)
                .build();

        // Calculate initial profile completion
        profile.calculateProfileCompletion();

        // Business rule: if profile marked completed but no contact number => invalid
        if (profile.getProfileCompleted() && profile.getContactNo() == null) {
            throw new BusinessRuleViolationException("Contact number required to complete profile");
        }



        // Assign default role
        final String defaultRole = "ROLE_GUEST";

        UserRole existing = userRoleRepository
                .findByKeycloakUserIdAndRoleNameIgnoreCase(userId, defaultRole);

        if (existing == null) {
            userRoleRepository.save(UserRole.builder()
                    .keycloakUserId(userId)
                    .roleName(defaultRole)
                    .build()
            );
            log.info("🎯 Default role '{}' assigned to {}", defaultRole, userId);
        } else {
            log.info("⚠️ Default role already exists for {} — skipping", userId);
        }

        // First save profile
        UserProfile saved = userProfileRepository.save(profile);
        log.info("📁 Profile saved for {}", userId);

        // 🔁 Return response with roles included
        Set<String> roles = userRoleService.getUserRoles(userId);

        UserProfileResponse response = userProfileMapper.toResponse(profile);
        response.setRoles(roles);

        return response;
    }

    public boolean profileExists(String userId) {
        return userProfileRepository.existsById(userId);
    }


    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileById(String userId) {
        log.debug("Fetching profile by ID: {}", userId);
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        Set<String> roles = userRoleService.getUserRoles(userId);

        UserProfileResponse response = userProfileMapper.toResponse(profile);
        response.setRoles(roles);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String username) {
        log.debug("Fetching profile by username: {}", username);
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        Set<String> roles = userRoleService.getUserRoles(profile.getId());

        UserProfileResponse response = userProfileMapper.toResponse(profile);
        response.setRoles(roles);

        return response;
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

        Set<String> roles = userRoleService.getUserRoles(userId);

        UserProfileResponse response = userProfileMapper.toResponse(profile);
        response.setRoles(roles);

        return response;
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

        Set<String> roles = userRoleService.getUserRoles(userId);

        UserProfileResponse response = userProfileMapper.toResponse(profile);
        response.setRoles(roles);

        return response;
    }

    @Override
    public void deleteProfile(String userId) {
        log.warn("Soft deleting profile for user: {}", userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        profile.setDeleted(true);
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
    public UserProfileResponse activateProfile(String userId) {
        log.info("Reactivating profile for user: {}", userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        profile.activate();
        UserProfile reactivatedProfile = userProfileRepository.save(profile);

        log.info("✅ Profile reactivated for user: {}", reactivatedProfile.getUsername());

        Set<String> roles = userRoleService.getUserRoles(userId);

        UserProfileResponse response = userProfileMapper.toResponse(profile);
        response.setRoles(roles);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> searchUsers(String searchTerm) {
        log.debug("Searching users with term: {}", searchTerm);
        List<UserProfile> profiles = userProfileRepository.searchUsers(searchTerm);
        return profiles.stream()
                .map(userProfileMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {

        List<UserProfile> users = userProfileRepository.findAll();

        List<String> userIds = users.stream()
                .map(UserProfile::getId)
                .toList();

        Map<String, Set<String>> rolesByUserId =
                userRoleRepository.findByKeycloakUserIdIn(userIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                UserRole::getKeycloakUserId,
                                Collectors.mapping(UserRole::getRoleName, Collectors.toSet())
                        ));

        return users.stream()
                .map(user -> {
                    UserProfileResponse response = userProfileMapper.toResponse(user);
                    response.setRoles(
                            rolesByUserId.getOrDefault(user.getId(), Set.of())
                    );
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersByOrganization(String organization) {
        log.debug("Fetching users by organization: {}", organization);
        List<UserProfile> profiles = userProfileRepository.findByOrganization(organization);
        return profiles.stream()
                .map(userProfileMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersWithIncompleteProfiles() {
        log.debug("Fetching users with incomplete profiles");
        List<UserProfile> profiles = userProfileRepository.findUsersWithIncompleteProfiles();
        return profiles.stream()
                .map(userProfileMapper::toResponse)
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
    public void logoutUser(UserLogoutEvent event) {
        UserProfile user = userProfileRepository.findById(event.getKeycloakUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + event.getKeycloakUserId()));
        user.onLogout();
    }

    @Override
    public void loginUser(UserLoginEvent event) {
        UserProfile user = userProfileRepository.findById(event.getKeycloakUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + event.getKeycloakUserId()));
        user.onLogin();
        userProfileRepository.save(user);
    }

    @Override
    public Integer getProfileCompletionPercentage(String userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return user.getProfileCompletionPercentage();
    }
}