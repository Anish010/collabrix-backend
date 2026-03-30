package com.collabrix.auth.service.interfaces;

import com.collabrix.auth.dto.UserRegisterRequest;
import com.collabrix.auth.dto.UserResponse;

public interface KeycloakUserService {
    UserResponse createUserOnly(UserRegisterRequest request);
    UserResponse getUserById(String userId);
    void deleteUser(String userId);
    void hardDeleteUser(String userId);
    UserResponse assignRoleToUser(String userId, String roleName);
    UserResponse removeRoleFromUser(String userId, String roleName);
}
