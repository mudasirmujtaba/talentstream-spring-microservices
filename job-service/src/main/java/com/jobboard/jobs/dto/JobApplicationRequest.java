package com.jobboard.jobs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationRequest {

    @NotBlank(message = "Cover letter is required")
    @Size(min = 50, max = 2000, message = "Cover letter must be between 50 and 2000 characters")
    private String coverLetter;

    private String resumeUrl;  // Optional: URL to uploaded resume
}