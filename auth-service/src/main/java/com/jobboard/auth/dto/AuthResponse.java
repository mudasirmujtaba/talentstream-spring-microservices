package com.jobboard.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String type = "Bearer"; // Token type
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String message;
}