package com.jobboard.jobs.service;

import com.jobboard.jobs.client.AuthServiceClient;
import com.jobboard.jobs.dto.JobApplicationRequest;
import com.jobboard.jobs.dto.JobApplicationResponse;
import com.jobboard.jobs.dto.*;
import com.jobboard.jobs.enums.ApplicationStatus;
import com.jobboard.jobs.exception.ApplicationAlreadyExistsException;
import com.jobboard.jobs.exception.JobNotFoundException;
import com.jobboard.jobs.exception.UnauthorizedAccessException;
import com.jobboard.jobs.model.Job;
import com.jobboard.jobs.model.JobApplication;
import com.jobboard.jobs.repository.JobApplicationRepository;
import com.jobboard.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final AuthServiceClient authServiceClient;

    private final MessagePublisher messagePublisher;

    @Transactional
    public JobApplicationResponse applyForJob(Long jobId, JobApplicationRequest request, String authToken) {
        log.info("User applying for job ID: {}", jobId);

        // Get current user
        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        // Validate user is not EMPLOYER
        if ("EMPLOYER".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("Employers cannot apply for jobs");
        }

        // Check if job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        // Check if job is active
        if (!job.isActive()) {
            throw new UnauthorizedAccessException("This job is no longer accepting applications");
        }

        // Check if user already applied
        if (jobApplicationRepository.existsByJobIdAndUserId(jobId, currentUser.getId())) {
            throw new ApplicationAlreadyExistsException("You have already applied for this job");
        }

        // Create application
        JobApplication application = JobApplication.builder()
                .jobId(jobId)
                .userId(currentUser.getId())
                .username(currentUser.getUsername())
                .userEmail(currentUser.getEmail())
                .coverLetter(request.getCoverLetter())
                .resumeUrl(request.getResumeUrl())
                .status(ApplicationStatus.PENDING)
                .build();

        JobApplication savedApplication = jobApplicationRepository.save(application);
        log.info("Application created successfully with ID: {}", savedApplication.getId());

        // Publish application submitted event
        ApplicationSubmittedEvent event = ApplicationSubmittedEvent.builder()
                .applicationId(savedApplication.getId())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .companyName(job.getCompanyName())
                .applicantUsername(currentUser.getUsername())
                .applicantEmail(currentUser.getEmail())
                .employerEmail(job.getPostedByEmail()) // In real app, fetch from User service
                .build();

        messagePublisher.publishApplicationSubmittedEvent(event);

        return mapToApplicationResponse(savedApplication, job);
    }

    public List<JobApplicationResponse> getApplicationsForJob(Long jobId, String authToken) {
        log.info("Fetching applications for job ID: {}", jobId);

        // Get current user
        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        // Check if job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        // Check if user is the job owner
        if (!job.getPostedByUserId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only view applications for your own jobs");
        }

        List<JobApplication> applications = jobApplicationRepository.findByJobId(jobId);

        return applications.stream()
                .map(app -> mapToApplicationResponse(app, job))
                .toList();
    }

    public List<JobApplicationResponse> getMyApplications(String authToken) {
        log.info("Fetching applications for current user");

        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        List<JobApplication> applications = jobApplicationRepository.findByUserId(currentUser.getId());

        return applications.stream()
                .map(app -> {
                    Job job = jobRepository.findById(app.getJobId())
                            .orElse(null);
                    return mapToApplicationResponse(app, job);
                })
                .toList();
    }

    @Transactional
    public JobApplicationResponse updateApplicationStatus(
            Long applicationId,
            ApplicationStatus status,
            String authToken) {

        log.info("Updating application ID {} to status {}", applicationId, status);

        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new JobNotFoundException("Application not found with ID: " + applicationId));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        // Check if user is the job owner
        if (!job.getPostedByUserId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only update applications for your own jobs");
        }

        application.setStatus(status);
        JobApplication updatedApplication = jobApplicationRepository.save(application);

        log.info("Application status updated successfully");

        // Publish status changed event
        ApplicationStatusChangedEvent event = ApplicationStatusChangedEvent.builder()
                .applicationId(updatedApplication.getId())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .companyName(job.getCompanyName())
                .applicantUsername(updatedApplication.getUsername())
                .applicantEmail(updatedApplication.getUserEmail())
                .oldStatus(application.getStatus().toString())
                .newStatus(status.toString())
                .build();

        messagePublisher.publishApplicationStatusChangedEvent(event);

        return mapToApplicationResponse(updatedApplication, job);
    }

    @Transactional
    public void withdrawApplication(Long jobId, String authToken) {
        log.info("Withdrawing application for job ID: {}", jobId);

        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        JobApplication application = jobApplicationRepository.findByJobIdAndUserId(jobId, currentUser.getId())
                .orElseThrow(() -> new JobNotFoundException("Application not found for this job"));

        application.setStatus(ApplicationStatus.WITHDRAWN);
        jobApplicationRepository.save(application);

        log.info("Application withdrawn successfully");
    }

    // Helper method
    private JobApplicationResponse mapToApplicationResponse(JobApplication application, Job job) {
        return JobApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJobId())
                .jobTitle(job != null ? job.getTitle() : "Unknown")
                .companyName(job != null ? job.getCompanyName() : "Unknown")
                .userId(application.getUserId())
                .username(application.getUsername())
                .userEmail(application.getUserEmail())
                .coverLetter(application.getCoverLetter())
                .resumeUrl(application.getResumeUrl())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}