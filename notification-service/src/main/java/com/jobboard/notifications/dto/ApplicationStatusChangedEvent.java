package com.jobboard.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusChangedEvent implements Serializable {
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String applicantUsername;
    private String applicantEmail;
    private String oldStatus;
    private String newStatus;
}