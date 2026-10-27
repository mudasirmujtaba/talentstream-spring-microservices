package com.jobboard.jobs.repository;

import com.jobboard.jobs.enums.ApplicationStatus;
import com.jobboard.jobs.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // Find all applications for a job
    List<JobApplication> findByJobId(Long jobId);

    // Find all applications by a user
    List<JobApplication> findByUserId(Long userId);

    // Check if user already applied
    boolean existsByJobIdAndUserId(Long jobId, Long userId);

    // Find specific application
    Optional<JobApplication> findByJobIdAndUserId(Long jobId, Long userId);

    // Find applications by status
    List<JobApplication> findByJobIdAndStatus(Long jobId, ApplicationStatus status);

    // Count applications for a job
    long countByJobId(Long jobId);

    // Count applications by user
    long countByUserId(Long userId);
}