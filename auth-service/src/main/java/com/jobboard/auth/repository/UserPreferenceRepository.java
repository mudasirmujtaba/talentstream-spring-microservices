package com.jobboard.auth.repository;

import com.jobboard.auth.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    Optional<UserPreference> findByUserId(Long userId);

    // Find all users who subscribed to a specific category and have notifications enabled
    @Query("SELECT p FROM UserPreference p WHERE :category MEMBER OF p.subscribedCategories AND p.emailNotificationsEnabled = true")
    List<UserPreference> findUsersSubscribedToCategory(@Param("category") String category);
}