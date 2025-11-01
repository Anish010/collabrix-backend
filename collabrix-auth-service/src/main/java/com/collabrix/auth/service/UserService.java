package com.collabrix.auth.service;

import com.collabrix.auth.dto.RegisterRequest;
import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.entity.Role;
import com.collabrix.auth.entity.User;
import com.collabrix.auth.repository.RoleRepository;
import com.collabrix.auth.repository.UserRepository;
import com.collabrix.common.libraries.exceptions.BusinessRuleViolationException;
import com.collabrix.common.libraries.exceptions.ResourceAlreadyExistsException;
import com.collabrix.common.libraries.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user with default "GUEST" role.
     */
    @Transactional
    public User register(RegisterRequest req) {
        log.info("Registering new user: {}", req.getUsername());

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already taken: " + req.getUsername());
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already in use: " + req.getEmail());
        }

        Role role = roleRepository.findByName("ROLE_GUEST")
                .orElseThrow(() -> new ResourceNotFoundException("Default role 'ROLE_GUEST' not found"));

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
                .roles(Set.of(role))
                .build();

        User savedUser = userRepository.save(user);
        log.info("✅ User '{}' registered successfully", savedUser.getUsername());
        return savedUser;
    }

    /**
     * Get all non-deleted users.
     */
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all active users...");

        try {
            List<UserResponse> users = userRepository.findAll().stream()
                    .filter(u -> !u.isDeleted())
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            if (users.isEmpty()) {
                throw new ResourceNotFoundException("No users found in the system");
            }

            return users;
        } catch (Exception ex) {
            log.error("Error fetching users: {}", ex.getMessage(), ex);
            throw new BusinessRuleViolationException("Unable to fetch users. Please try again later.");
        }
    }

    /**
     * Get user by ID.
     */
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return toResponse(user);
    }

    /**
     * Update user basic info.
     */
    @Transactional
    public UserResponse updateUser(UUID id, User updatedUser) {
        log.info("Updating user details for ID: {}", id);
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (!user.isActive()) {
            throw new BusinessRuleViolationException("Cannot update an inactive user");
        }

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setOrganization(updatedUser.getOrganization());
        user.setContactNo(updatedUser.getContactNo());
        user.setCountryCode(updatedUser.getCountryCode());

        userRepository.save(user);
        log.info("✅ Updated user: {}", user.getUsername());
        return toResponse(user);
    }

    /**
     * Soft delete a user (mark as deleted and inactive).
     */
    @Transactional
    public void deleteUser(UUID id) {
        log.warn("Soft deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);

        log.info("🟠 Soft deleted user: {}", user.getUsername());
    }

    /**
     * Permanently delete a user from the database (hard delete).
     */
    @Transactional
    public void hardDeleteUser(UUID id) {
        log.error("Hard deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userRepository.delete(user);
        log.warn("⚠️ User permanently deleted: {}", user.getUsername());
    }

    /**
     * Assign a new role to the user.
     */
    @Transactional
    public UserResponse assignRole(UUID id, String roleName) {
        log.info("Assigning role '{}' to user with ID: {}", roleName, id);

        // ✅ Fetch active (non-deleted) user
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // ✅ Fetch the role by name
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        // ✅ Prevent duplicate role assignment
        if (user.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(roleName))) {
            throw new BusinessRuleViolationException("User already has role: " + roleName);
        }

        // ✅ Add role to user
        user.getRoles().add(role);

        // ✅ Save user (transaction ensures atomicity)
        userRepository.save(user);

        log.info("✅ Successfully assigned role '{}' to user '{}'", roleName, user.getUsername());

        return toResponse(user);
    }

    /**
     * Convert User entity to response DTO.
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .contactNo(user.getContactNo())
                .organization(user.getOrganization())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
