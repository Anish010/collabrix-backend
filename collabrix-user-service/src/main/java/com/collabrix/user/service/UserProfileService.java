package com.collabrix.user.service;

import com.collabrix.common.libraries.dto.UserProfileCreateRequest;
import com.collabrix.common.libraries.events.UserLoginEvent;
import com.collabrix.common.libraries.events.UserLogoutEvent;
import com.collabrix.user.dto.UpdateProfileRequest;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.dto.UserStatisticsResponse;
import java.util.List;

/**
 * Service interface for user profile operations
 */
public interface UserProfileService {

    UserProfileResponse createProfile(UserProfileCreateRequest req);

    boolean profileExists(String userId);

    UserProfileResponse getProfileById(String userId);

    UserProfileResponse getProfileByUsername(String username);

    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);

    UserProfileResponse updateAvatar(String userId, String avatarUrl);

    void deleteProfile(String userId);

    void hardDeleteProfile(String userId);

    UserProfileResponse activateProfile(String userId);

    List<UserProfileResponse> searchUsers(String searchTerm);

    List<UserProfileResponse> getAllUsers();

    List<UserProfileResponse> getUsersByOrganization(String organization);

    List<UserProfileResponse> getUsersWithIncompleteProfiles();

    UserStatisticsResponse getUserStatistics();

    void updateLastLogin(String userId);


    void logoutUser(UserLogoutEvent event);
    void loginUser(UserLoginEvent event);

    Integer getProfileCompletionPercentage(String userId);
}