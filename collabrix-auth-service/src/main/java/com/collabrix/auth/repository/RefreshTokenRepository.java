package com.collabrix.auth.repository;

import com.collabrix.auth.entity.RefreshToken;
import com.collabrix.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    boolean existsByUser(User user);
}
