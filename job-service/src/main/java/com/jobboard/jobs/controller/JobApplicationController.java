package com.jobboard.jobs.controller;

import com.jobboard.jobs.dto.JobApplicationRequest;
import com.jobboard.jobs.dto.JobApplicationResponse;
import com.jobboard.jobs.enums.ApplicationStatus;
import com.jobboard.jobs.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{jobId}/apply")
    public ResponseEntity<JobApplicationResponse> applyForJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobApplicationRequest request,
            @RequestHeader("Authorization") String authToken) {

        log.info("Apply for job request - Job ID: {}", jobId);
        JobApplicationResponse response = jobApplicationService.applyForJob(jobId, request, authToken);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{jobId}/applications")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String authToken) {

        log.info("Get applications for job ID: {}", jobId);
        List<JobApplicationResponse> applications = jobApplicationService.getApplicationsForJob(jobId, authToken);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/applications/my-applications")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications(
            @RequestHeader("Authorization") String authToken) {

        log.info("Get my applications request");
        List<JobApplicationResponse> applications = jobApplicationService.getMyApplications(authToken);
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status,
            @RequestHeader("Authorization") String authToken) {

        log.info("Update application status request - ID: {}, Status: {}", applicationId, status);
        JobApplicationResponse response = jobApplicationService.updateApplicationStatus(
                applicationId, status, authToken);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{jobId}/applications/withdraw")
    public ResponseEntity<String> withdrawApplication(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String authToken) {

        log.info("Withdraw application for job ID: {}", jobId);
        jobApplicationService.withdrawApplication(jobId, authToken);
        return ResponseEntity.ok("Application withdrawn successfully");
    }
}