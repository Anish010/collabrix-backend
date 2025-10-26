package com.collabrix.auth.repository;

import com.collabrix.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User repository.
 */
/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom finder methods.
 * All methods return Optional to handle absence of a record safely.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their username.
     *
     * @param username the username to search for
     * @return Optional containing User if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     *
     * @param email the email to search for
     * @return Optional containing User if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

}