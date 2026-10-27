package com.jobboard.jobs.dto;

import com.jobboard.jobs.enums.ExperienceLevel;
import com.jobboard.jobs.enums.JobCategory;
import com.jobboard.jobs.enums.JobType;
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
public class JobResponse {

    private Long id;
    private String title;
    private String description;
    private String companyName;
    private String location;
    private JobType jobType;
    private JobCategory category;
    private ExperienceLevel experienceLevel;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String skillsRequired;
    private Long postedByUserId;
    private String postedByUsername;
    private boolean isActive;
    private LocalDateTime applicationDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long applicationCount;  // Number of applications
}