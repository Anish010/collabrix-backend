package com.collabrix.auth.service;

import com.collabrix.auth.dto.RegisterRequest;
import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.entity.Role;
import com.collabrix.auth.entity.User;
import com.collabrix.auth.exception.ResourceAlreadyExistsException;
import com.collabrix.auth.repository.RoleRepository;
import com.collabrix.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest req) {
        log.info("Attempting registration for username: {}", req.getUsername());

        userRepository.findByUsername(req.getUsername()).ifPresent(u -> {
            throw new ResourceAlreadyExistsException("Username already taken");
        });

        userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new ResourceAlreadyExistsException("Email already in use");
        });

        Role userRole = roleRepository.findByName("GUEST")
                .orElseGet(() -> {
                    // Auto-bootstrap default role
                    Role r = Role.builder().name("GUEST").systemDefined(true).build();
                    r = roleRepository.save(r);
                    log.info("Created missing role GUEST");
                    return r;
                });

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .countryCode(req.getCountryCode())
                .contactNo(req.getContactNo())
                .organization(req.getOrganization())
                .active(true)
                .roles(Set.of(userRole))
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());
        return saved;
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .contactNo(user.getContactNo())
                .organization(user.getOrganization())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    /**
     * Fetch user by username.
     *
     * @param username the username to search
     * @return the User entity
     */
    public User getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new ResourceAlreadyExistsException("User not found with username: " + username);
                });
    }

    public User getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new ResourceAlreadyExistsException("User not found with email: " + email);
                });
    }

}
