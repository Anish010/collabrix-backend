package com.collabrix.auth.repository;

import com.collabrix.auth.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByRefreshToken(String refreshToken);

    List<AuthSession> findByKeycloakUserIdAndValidTrue(String keycloakUserId);

    void deleteByKeycloakUserId(String keycloakUserId);
}