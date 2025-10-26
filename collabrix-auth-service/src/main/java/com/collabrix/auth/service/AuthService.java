package com.collabrix.auth.service;

import com.collabrix.auth.dto.JwtResponse;
import com.collabrix.auth.dto.LoginRequest;
import com.collabrix.auth.dto.RegisterRequest;
import com.collabrix.auth.entity.RefreshToken;
import com.collabrix.auth.entity.User;
import com.collabrix.auth.repository.UserRepository;
import com.collabrix.auth.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service responsible for all authentication operations:
 * - user registration
 * - login and token generation
 * - refresh token flow
 */
@Service
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtTokenProvider tokenProvider,
                       RefreshTokenService refreshTokenService,
                       UserService userService,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user by delegating to UserService.
     */
    public User register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        return userService.register(request);
    }

    /**
     * Authenticates a user and generates JWT access + refresh tokens.
     */
    public JwtResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = tokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        log.info("User {} authenticated successfully", userDetails.getUsername());

        return new JwtResponse(accessToken, refreshToken.getToken(), "Bearer");
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * Rotates refresh token for security.
     */
    public JwtResponse refreshToken(String refreshTokenStr) {
        log.info("Refreshing access token using refresh token");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshTokenService.isExpired(refreshToken)) {
            refreshTokenService.deleteByUser(refreshToken.getUser());
            throw new BadCredentialsException("Refresh token expired. Please login again.");
        }

        CustomUserDetails userDetails = new CustomUserDetails(refreshToken.getUser());
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);

        // Rotate refresh token: delete old and issue a new one
        refreshTokenService.deleteByUser(refreshToken.getUser());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        log.info("Refresh token rotated successfully for user {}", userDetails.getUsername());

        return new JwtResponse(newAccessToken, newRefreshToken.getToken(), "Bearer");
    }
}
