package com.jobboard.jobs.service;

import com.jobboard.jobs.client.AuthServiceClient;
import com.jobboard.jobs.dto.*;
import com.jobboard.jobs.enums.JobCategory;
import com.jobboard.jobs.enums.JobType;
import com.jobboard.jobs.exception.InvalidJobDataException;
import com.jobboard.jobs.exception.JobNotFoundException;
import com.jobboard.jobs.exception.UnauthorizedAccessException;
import com.jobboard.jobs.model.Job;
import com.jobboard.jobs.repository.JobApplicationRepository;
import com.jobboard.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuthServiceClient authServiceClient;

    private final MessagePublisher messagePublisher;

    @Transactional
    public JobResponse createJob(JobRequest request, String authToken) {
        log.info("Creating new job: {}", request.getTitle());

        // Get current user from Auth Service
        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        // Validate user is EMPLOYER
        if (!"EMPLOYER".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("Only employers can create jobs");
        }

        // Validate salary range
        if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
            if (request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
                throw new InvalidJobDataException("Minimum salary cannot be greater than maximum salary");
            }
        }

        // Create job
        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .companyName(request.getCompanyName())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .category(request.getCategory())
                .experienceLevel(request.getExperienceLevel())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .skillsRequired(request.getSkillsRequired())
                .postedByUserId(currentUser.getId())
                .postedByUsername(currentUser.getUsername())
                .postedByEmail(currentUser.getEmail())  // ← Fixed: actual email
                .applicationDeadline(request.getApplicationDeadline())
                .isActive(true)
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with ID: {}", savedJob.getId());

        // ============================================
        // PUBLISH JOB POSTED EVENT - UPDATED SECTION
        // ============================================
        try {
            // Fetch users subscribed to this job's category
            String category = savedJob.getCategory().toString();
            log.info("Fetching users subscribed to category: {}", category);

            List<UserDTO> subscribedUsers = authServiceClient.getUsersSubscribedToCategory(category);

            List<String> subscribedEmails = subscribedUsers.stream()
                    .map(UserDTO::getEmail)
                    .toList();

            log.info("Found {} subscribed users for category {}", subscribedEmails.size(), category);

            JobPostedEvent event = JobPostedEvent.builder()
                    .jobId(savedJob.getId())
                    .title(savedJob.getTitle())
                    .companyName(savedJob.getCompanyName())
                    .location(savedJob.getLocation())
                    .category(category)
                    .salaryMin(savedJob.getSalaryMin())
                    .salaryMax(savedJob.getSalaryMax())
                    .postedByUsername(currentUser.getUsername())
                    .postedByEmail(currentUser.getEmail())
                    .jobSeekerEmails(subscribedEmails)  // Only subscribed users!
                    .build();

            messagePublisher.publishJobPostedEvent(event);

            log.info("Job posted event published successfully");

        } catch (Exception e) {
            log.error("Failed to publish job posted event: {}", e.getMessage());
            // Don't fail job creation if notification fails
        }

        return mapToJobResponse(savedJob);
    }

    public JobResponse getJobById(Long jobId) {
        log.info("Fetching job with ID: {}", jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        return mapToJobResponse(job);
    }

    public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
        log.info("Fetching all active jobs");

        Page<Job> jobs = jobRepository.findByIsActiveTrue(pageable);
        return jobs.map(this::mapToJobResponse);
    }

    public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
        log.info("Searching jobs with keyword: {}", keyword);

        Page<Job> jobs = jobRepository.searchJobs(keyword, pageable);
        return jobs.map(this::mapToJobResponse);
    }

    public Page<JobResponse> getJobsByCategory(JobCategory category, Pageable pageable) {
        log.info("Fetching jobs by category: {}", category);

        Page<Job> jobs = jobRepository.findByCategoryAndIsActiveTrue(category, pageable);
        return jobs.map(this::mapToJobResponse);
    }

    public Page<JobResponse> getJobsByType(JobType jobType, Pageable pageable) {
        log.info("Fetching jobs by type: {}", jobType);

        Page<Job> jobs = jobRepository.findByJobTypeAndIsActiveTrue(jobType, pageable);
        return jobs.map(this::mapToJobResponse);
    }

    public Page<JobResponse> getJobsBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary, Pageable pageable) {
        log.info("Fetching jobs with salary range: {} - {}", minSalary, maxSalary);

        Page<Job> jobs = jobRepository.findBySalaryRange(minSalary, maxSalary, pageable);
        return jobs.map(this::mapToJobResponse);
    }

    public List<JobResponse> getMyJobs(String authToken) {
        log.info("Fetching jobs for current employer");

        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        if (!"EMPLOYER".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("Only employers can view their jobs");
        }

        List<Job> jobs = jobRepository.findByPostedByUserId(currentUser.getId());
        return jobs.stream()
                .map(this::mapToJobResponse)
                .toList();
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, String authToken) {
        log.info("Updating job with ID: {}", jobId);

        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        // Check if user is the owner of the job
        if (!job.getPostedByUserId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only update your own jobs");
        }

        // Validate salary range
        if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
            if (request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
                throw new InvalidJobDataException("Minimum salary cannot be greater than maximum salary");
            }
        }

        // Update job fields
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompanyName(request.getCompanyName());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        job.setCategory(request.getCategory());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setSkillsRequired(request.getSkillsRequired());
        job.setApplicationDeadline(request.getApplicationDeadline());

        Job updatedJob = jobRepository.save(job);
        log.info("Job updated successfully: {}", updatedJob.getId());

        return mapToJobResponse(updatedJob);
    }

    @Transactional
    public void deleteJob(Long jobId, String authToken) {
        log.info("Deleting job with ID: {}", jobId);

        UserDTO currentUser = authServiceClient.getCurrentUser(authToken);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        // Check if user is the owner of the job
        if (!job.getPostedByUserId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You can only delete your own jobs");
        }

        // Soft delete (set isActive to false)
        job.setActive(false);
        jobRepository.save(job);

        log.info("Job deleted successfully: {}", jobId);
    }

    // Helper method to map Job to JobResponse
    private JobResponse mapToJobResponse(Job job) {
        long applicationCount = jobApplicationRepository.countByJobId(job.getId());

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .companyName(job.getCompanyName())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .category(job.getCategory())
                .experienceLevel(job.getExperienceLevel())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skillsRequired(job.getSkillsRequired())
                .postedByUserId(job.getPostedByUserId())
                .postedByUsername(job.getPostedByUsername())
                .isActive(job.isActive())
                .applicationDeadline(job.getApplicationDeadline())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .applicationCount(applicationCount)
                .build();
    }
}