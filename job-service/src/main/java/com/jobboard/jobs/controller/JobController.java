package com.jobboard.jobs.controller;

import com.jobboard.jobs.dto.JobRequest;
import com.jobboard.jobs.dto.JobResponse;
import com.jobboard.jobs.enums.JobCategory;
import com.jobboard.jobs.enums.JobType;
import com.jobboard.jobs.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request,
            @RequestHeader("Authorization") String authToken) {

        log.info("Create job request received");
        JobResponse response = jobService.createJob(request, authToken);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long jobId) {
        log.info("Get job by ID request: {}", jobId);
        JobResponse response = jobService.getJobById(jobId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get all jobs request - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<JobResponse> jobs = jobService.getAllActiveJobs(pageable);

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Search jobs request with keyword: {}", keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.searchJobs(keyword, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<JobResponse>> getJobsByCategory(
            @PathVariable JobCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Get jobs by category: {}", category);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.getJobsByCategory(category, pageable);

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/type/{jobType}")
    public ResponseEntity<Page<JobResponse>> getJobsByType(
            @PathVariable JobType jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Get jobs by type: {}", jobType);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.getJobsByType(jobType, pageable);

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/salary-range")
    public ResponseEntity<Page<JobResponse>> getJobsBySalaryRange(
            @RequestParam BigDecimal minSalary,
            @RequestParam BigDecimal maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Get jobs by salary range: {} - {}", minSalary, maxSalary);

        Pageable pageable = PageRequest.of(page, size, Sort.by("salaryMin").ascending());
        Page<JobResponse> jobs = jobService.getJobsBySalaryRange(minSalary, maxSalary, pageable);

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<List<JobResponse>> getMyJobs(
            @RequestHeader("Authorization") String authToken) {

        log.info("Get my jobs request");
        List<JobResponse> jobs = jobService.getMyJobs(authToken);
        return ResponseEntity.ok(jobs);
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request,
            @RequestHeader("Authorization") String authToken) {

        log.info("Update job request for ID: {}", jobId);
        JobResponse response = jobService.updateJob(jobId, request, authToken);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<String> deleteJob(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String authToken) {

        log.info("Delete job request for ID: {}", jobId);
        jobService.deleteJob(jobId, authToken);
        return ResponseEntity.ok("Job deleted successfully");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Job Service is running!");
    }
}