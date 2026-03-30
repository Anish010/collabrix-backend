package com.collabrix.user.repository;

import com.collabrix.user.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, String> {
    List<UserActivity> findByKeycloakUserIdOrderByTimestampDesc(String userId);
}
