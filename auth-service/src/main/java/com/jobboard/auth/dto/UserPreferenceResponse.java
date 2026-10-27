package com.jobboard.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceResponse {
    private Long id;
    private Long userId;
    private Set<String> subscribedCategories;
    private boolean emailNotificationsEnabled;
}