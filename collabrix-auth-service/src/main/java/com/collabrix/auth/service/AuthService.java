package com.collabrix.auth.service;

import com.collabrix.auth.dto.JwtResponse;
import com.collabrix.auth.dto.LoginRequest;
import com.collabrix.auth.dto.RegisterRequest;
import com.collabrix.auth.entity.RefreshToken;
import com.collabrix.auth.entity.User;
import com.collabrix.auth.repository.UserRepository;
import com.collabrix.auth.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * High-level authentication operations:
 * - register
 * - login (authenticate + generate tokens)
 * - refresh tokens using stored refresh token
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

    public User register(RegisterRequest req) {
        // Delegate to UserService
        return userService.register(req);
    }

    public JwtResponse login(LoginRequest req) {
        log.info("Attempting login for: {}", req.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        // If authentication fails, exception is thrown by AuthenticationManager
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var custom = (CustomUserDetails) userDetails;

        String accessToken = tokenProvider.generateAccessToken(custom);
        // create and store refresh token in DB
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(custom.getUsername());

        log.info("User {} authenticated successfully", custom.getUsername());
        return new JwtResponse(accessToken, refreshToken.getToken(), "Bearer");
    }

    public JwtResponse refreshToken(String refreshTokenStr) {
        log.info("Attempting refresh token flow");
        var opt = refreshTokenService.findByToken(refreshTokenStr);
        if (opt.isEmpty()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        RefreshToken refreshToken = opt.get();
        if (refreshTokenService.isExpired(refreshToken)) {
            refreshTokenService.deleteByUser(refreshToken.getUser()); // revoke
            throw new BadCredentialsException("Refresh token expired, please login again");
        }

        CustomUserDetails cud = new CustomUserDetails(refreshToken.getUser());
        String newAccessToken = tokenProvider.generateAccessToken(cud);

        // Optionally rotate refresh token: delete old and create new
        refreshTokenService.deleteByUser(refreshToken.getUser());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(cud.getUsername());

        log.info("Refresh token rotated for user {}", cud.getUsername());
        return new JwtResponse(newAccessToken, newRefreshToken.getToken(), "Bearer");
    }
}
