package com.jobboard.jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostedEvent implements Serializable {
    private Long jobId;
    private String title;
    private String companyName;
    private String location;
    private String category;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String postedByUsername;
    private String postedByEmail;
    private List<String> jobSeekerEmails;  // Subscribed users only
}