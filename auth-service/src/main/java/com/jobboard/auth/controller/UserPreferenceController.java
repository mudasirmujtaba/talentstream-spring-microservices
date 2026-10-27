package com.jobboard.auth.controller;

import com.jobboard.auth.dto.SubscribedUserDTO;
import com.jobboard.auth.dto.UserPreferenceRequest;
import com.jobboard.auth.dto.UserPreferenceResponse;
import com.jobboard.auth.exception.UserNotFoundException;
import com.jobboard.auth.model.User;
import com.jobboard.auth.repository.UserRepository;
import com.jobboard.auth.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/preferences")
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;
    private final UserRepository userRepository;

    @PutMapping
    public ResponseEntity<UserPreferenceResponse> updatePreferences(
            @RequestBody UserPreferenceRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Long userId = getUserIdFromUsername(username);

        log.info("Update preferences request for user: {} (ID: {})", username, userId);
        UserPreferenceResponse response = preferenceService.updatePreferences(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<UserPreferenceResponse> getMyPreferences() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Long userId = getUserIdFromUsername(username);

        log.info("Get preferences request for user: {} (ID: {})", username, userId);
        UserPreferenceResponse response = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(response);
    }

    // Public endpoint for Job Service to query
    @GetMapping("/subscribed-users/{category}")
    public ResponseEntity<List<SubscribedUserDTO>> getSubscribedUsers(
            @PathVariable String category) {

        log.info("Getting users subscribed to category: {}", category);
        List<SubscribedUserDTO> users = preferenceService.getUsersSubscribedToCategory(category);
        log.info("Found {} users subscribed to {}", users.size(), category);
        return ResponseEntity.ok(users);
    }

    // Helper method - FIXED
    private Long getUserIdFromUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return user.getId();
    }
}