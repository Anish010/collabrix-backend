package com.collabrix.auth.service;

import com.collabrix.auth.entity.User;
import com.collabrix.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Loads a user from DB and converts to CustomUserDetails for AuthenticationManager.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            log.debug("User not found: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User user = opt.get();
        log.debug("Loaded user {} from DB", username);
        return new CustomUserDetails(user);
    }
}
