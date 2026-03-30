package com.collabrix.user.service;

import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.entity.UserProfile;
import com.collabrix.user.entity.UserRole;
import com.collabrix.user.exception.UserNotFoundException;
import com.collabrix.user.mapper.UserProfileMapper;
import com.collabrix.user.repository.UserProfileRepository;
import com.collabrix.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleServiceImpl implements UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public Set<String> getUserRoles(String keycloakUserId) {
        return userRoleRepository.findByKeycloakUserId(keycloakUserId)
                .stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.toSet());
    }

    @Override
    public UserProfileResponse addRole(String keycloakUserId, String role) {
        log.info("Adding role '{}' to user {}", role, keycloakUserId);

        UserProfile profile = userProfileRepository.findById(keycloakUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + keycloakUserId));

        String normalizedRole = role.toUpperCase();
        UserRole existing = userRoleRepository.findByKeycloakUserIdAndRoleNameIgnoreCase(keycloakUserId, normalizedRole);

        if (existing == null) {
            userRoleRepository.save(
                    UserRole.builder()
                            .keycloakUserId(keycloakUserId)
                            .roleName(normalizedRole)
                            .build()
            );
            log.info("✅ Role '{}' persisted for user {}", normalizedRole, keycloakUserId);
        } else {
            log.info("ℹ️ Role '{}' already exists for user {}", normalizedRole, keycloakUserId);
        }

        Set<String> roles = getUserRoles(keycloakUserId);

        UserProfileResponse response = userProfileMapper.toResponse(profile);
        response.setRoles(roles);

        return response;
    }

    @Override
    public UserProfileResponse removeRole(String userId, String role) {
        log.info("Removing role '{}' from user {}", role, userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String normalizedRole = role.toUpperCase();

        UserRole existing = userRoleRepository.findByKeycloakUserIdAndRoleNameIgnoreCase(userId, normalizedRole);
        if (existing != null) {
            userRoleRepository.delete(existing);
            log.info("🗑️ Role '{}' removed from user {}", normalizedRole, userId);
        } else {
            log.info("ℹ️ User {} did not have role '{}'", userId, normalizedRole);
        }

        Set<String> roles = getUserRoles(userId);

        UserProfileResponse response = userProfileMapper.toResponse(profile);
        response.setRoles(roles);

        return response;
    }

}
