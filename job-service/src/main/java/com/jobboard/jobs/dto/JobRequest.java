package com.jobboard.jobs.dto;

import com.jobboard.jobs.enums.ExperienceLevel;
import com.jobboard.jobs.enums.JobCategory;
import com.jobboard.jobs.enums.JobType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    private String description;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotNull(message = "Category is required")
    private JobCategory category;

    @NotNull(message = "Experience level is required")
    private ExperienceLevel experienceLevel;

    @Positive(message = "Minimum salary must be positive")
    private BigDecimal salaryMin;

    @Positive(message = "Maximum salary must be positive")
    private BigDecimal salaryMax;

    private String skillsRequired;  // Comma-separated skills

    private LocalDateTime applicationDeadline;
}