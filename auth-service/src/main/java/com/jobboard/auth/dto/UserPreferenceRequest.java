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
public class UserPreferenceRequest {
    private Set<String> subscribedCategories;
    private boolean emailNotificationsEnabled;
}