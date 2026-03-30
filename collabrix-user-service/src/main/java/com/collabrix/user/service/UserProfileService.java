package com.collabrix.user.service;

import com.collabrix.user.dto.UpdateProfileRequest;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.dto.UserStatisticsResponse;
import com.collabrix.user.kafka.events.UserRegisteredEvent;

import java.util.List;

/**
 * Service interface for user profile operations
 */
public interface UserProfileService {

    UserProfileResponse createProfile(UserRegisteredEvent event);

    UserProfileResponse getProfileById(String userId);

    UserProfileResponse getProfileByUsername(String username);

    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);

    UserProfileResponse updateAvatar(String userId, String avatarUrl);

    void deleteProfile(String userId);

    void hardDeleteProfile(String userId);

    UserProfileResponse reactivateProfile(String userId);

    List<UserProfileResponse> searchUsers(String searchTerm);

    List<UserProfileResponse> getAllActiveUsers();

    List<UserProfileResponse> getUsersByOrganization(String organization);

    List<UserProfileResponse> getUsersWithIncompleteProfiles();

    UserStatisticsResponse getUserStatistics();

    void updateLastLogin(String userId);

    UserProfileResponse addRole(String userId, String role);

    UserProfileResponse removeRole(String userId, String role);

}