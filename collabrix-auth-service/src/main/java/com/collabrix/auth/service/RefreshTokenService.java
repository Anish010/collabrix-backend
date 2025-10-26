package com.collabrix.auth.service;

import com.collabrix.auth.entity.RefreshToken;
import com.collabrix.auth.entity.User;
import com.collabrix.auth.repository.RefreshTokenRepository;
import com.collabrix.auth.repository.UserRepository;
import com.collabrix.auth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Creates and validates refresh tokens stored in DB for revocation.
 */
@Service
public class RefreshTokenService {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${JWT_REFRESH_EXPIRATION}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(jwtTokenProvider.generateRefreshToken(new CustomUserDetails(user)))
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        RefreshToken saved = refreshTokenRepository.save(rt);
        log.debug("Created refresh token for user: {}", username);
        return saved;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.debug("Deleted all refresh tokens for user: {}", user.getUsername());
    }

    public boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }
}
