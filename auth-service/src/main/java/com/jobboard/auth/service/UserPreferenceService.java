package com.jobboard.auth.service;

import com.jobboard.auth.dto.SubscribedUserDTO;
import com.jobboard.auth.dto.UserPreferenceRequest;
import com.jobboard.auth.dto.UserPreferenceResponse;
import com.jobboard.auth.model.User;
import com.jobboard.auth.model.UserPreference;
import com.jobboard.auth.repository.UserPreferenceRepository;
import com.jobboard.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserPreferenceResponse updatePreferences(Long userId, UserPreferenceRequest request) {
        log.info("Updating preferences for user: {}", userId);

        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(UserPreference.builder()
                        .userId(userId)
                        .subscribedCategories(new HashSet<>())
                        .build());

        preference.setSubscribedCategories(request.getSubscribedCategories());
        preference.setEmailNotificationsEnabled(request.isEmailNotificationsEnabled());

        UserPreference saved = preferenceRepository.save(preference);

        return mapToResponse(saved);
    }

    public UserPreferenceResponse getPreferences(Long userId) {
        log.info("Fetching preferences for user: {}", userId);

        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(UserPreference.builder()
                        .userId(userId)
                        .subscribedCategories(new HashSet<>())
                        .emailNotificationsEnabled(true)
                        .build());

        return mapToResponse(preference);
    }

    public List<SubscribedUserDTO> getUsersSubscribedToCategory(String category) {
        log.info("Fetching users subscribed to category: {}", category);

        List<UserPreference> preferences = preferenceRepository
                .findUsersSubscribedToCategory(category);

        return preferences.stream()
                .map(pref -> {
                    User user = userRepository.findById(pref.getUserId()).orElse(null);
                    if (user == null) return null;

                    return SubscribedUserDTO.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .username(user.getUsername())
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    private UserPreferenceResponse mapToResponse(UserPreference preference) {
        return UserPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .subscribedCategories(preference.getSubscribedCategories())
                .emailNotificationsEnabled(preference.isEmailNotificationsEnabled())
                .build();
    }
}