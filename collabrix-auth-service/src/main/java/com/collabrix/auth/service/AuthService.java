package com.collabrix.auth.service;

import com.collabrix.auth.dto.JwtResponse;
import com.collabrix.auth.dto.LoginRequest;
import com.collabrix.auth.dto.RegisterRequest;
import com.collabrix.auth.entity.RefreshToken;
import com.collabrix.auth.entity.Role;
import com.collabrix.auth.entity.User;
import com.collabrix.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Register a new user.
     */
    public JwtResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());

        User user = userService.register(request);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRoles().stream().findFirst().map(Role::getName).orElse("ROLE_GUEST"))
                .build();
    }

    /**
     * Login user and generate access + refresh tokens.
     */
    public JwtResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .username(userDetails.getUsername())
                .email(userDetails.getUser().getEmail()) // ✅ FIXED
                .role(
                        userDetails.getUser()
                                .getRoles()
                                .stream()
                                .findFirst()
                                .map(Role::getName)
                                .orElse("ROLE_GUEST")
                )
                .build();
    }


    /**
     * Refresh access token using a valid refresh token.
     */
    public JwtResponse refreshToken(String oldRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.findByToken(oldRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshTokenService.isExpired(refreshToken)) {
            refreshTokenService.deleteByUser(refreshToken.getUser());
            throw new BadCredentialsException("Refresh token expired, please login again.");
        }

        CustomUserDetails userDetails = new CustomUserDetails(refreshToken.getUser());
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        refreshTokenService.deleteByUser(refreshToken.getUser());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .username(userDetails.getUsername())
                .email(userDetails.getUser().getEmail())
                .role(userDetails.getUser().getRoles().stream().findFirst().map(Role::getName).orElse("GUEST"))
                .build();
    }
}