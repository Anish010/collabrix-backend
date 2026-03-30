package com.collabrix.user.service;

import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.entity.UserRole;

import java.util.Set;

public interface UserRoleService {
    Set<String> getUserRoles(String keycloakUserId);
    UserProfileResponse addRole(String keycloakUserId, String role);
    UserProfileResponse removeRole(String keycloakUserId, String role);
}
