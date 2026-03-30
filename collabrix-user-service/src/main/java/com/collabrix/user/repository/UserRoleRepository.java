package com.collabrix.user.repository;

import com.collabrix.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    List<UserRole> findByKeycloakUserId(String keycloakUserId);

    List<UserRole> findByKeycloakUserIdIn(List<String> keycloakUserIds);

    UserRole findByKeycloakUserIdAndRoleNameIgnoreCase(String keycloakUserId, String roleName);

}
