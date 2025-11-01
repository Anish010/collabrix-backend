package com.collabrix.auth.service;

import com.collabrix.auth.dto.JwtResponse;
import com.collabrix.auth.entity.RefreshToken;
import com.collabrix.auth.entity.Role;
import com.collabrix.auth.entity.User;
import com.collabrix.auth.repository.RefreshTokenRepository;
import com.collabrix.auth.repository.UserRepository;
import com.collabrix.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Creates and validates refresh tokens stored in DB for revocation.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${JWT_REFRESH_EXPIRATION}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Create a new refresh token for a given username.
     */
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found: " + username));

        // Delete existing token for the user (one active refresh token policy)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Find refresh token by token string.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Check if a refresh token is expired.
     */
    public boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }

    /**
     * Delete all refresh tokens for a given user.
     */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Delete refresh token by token string.
     */
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Generate a new access token using a valid refresh token.
     */
    @Transactional
    public JwtResponse refreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = findByToken(requestRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (isExpired(refreshToken)) {
            deleteByUser(refreshToken.getUser());
            throw new BadCredentialsException("Refresh token expired. Please login again.");
        }

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        // rotate refresh token (new one each time)
        deleteByUser(user);
        RefreshToken newRefreshToken = createRefreshToken(user.getUsername());

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRoles().stream().findFirst().map(Role::getName).orElse("GUEST"))
                .build();
    }

}
