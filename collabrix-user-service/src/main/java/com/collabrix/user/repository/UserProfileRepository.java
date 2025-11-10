package com.collabrix.user.repository;

import com.collabrix.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserProfile entity
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    Optional<UserProfile> findByUsername(String username);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<UserProfile> findByActiveTrue();

    List<UserProfile> findByOrganization(String organization);

    @Query("SELECT u FROM UserProfile u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UserProfile> searchUsers(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM UserProfile u WHERE u.profileCompleted = false AND u.active = true")
    List<UserProfile> findUsersWithIncompleteProfiles();

    List<UserProfile> findByCountryCode(String countryCode);

    long countByActiveTrue();

    long countByProfileCompletedTrue();
}