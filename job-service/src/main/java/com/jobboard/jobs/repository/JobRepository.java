package com.jobboard.jobs.repository;

import com.jobboard.jobs.enums.JobCategory;
import com.jobboard.jobs.enums.JobType;
import com.jobboard.jobs.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Find active jobs
    Page<Job> findByIsActiveTrue(Pageable pageable);

    // Find jobs by employer
    List<Job> findByPostedByUserId(Long userId);

    // Find by category
    Page<Job> findByCategoryAndIsActiveTrue(JobCategory category, Pageable pageable);

    // Find by job type
    Page<Job> findByJobTypeAndIsActiveTrue(JobType jobType, Pageable pageable);

    // Search by title or company (case-insensitive)
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
            "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);

    // Find jobs within salary range
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
            "j.salaryMin >= :minSalary AND j.salaryMax <= :maxSalary")
    Page<Job> findBySalaryRange(@Param("minSalary") BigDecimal minSalary,
                                @Param("maxSalary") BigDecimal maxSalary,
                                Pageable pageable);

    // Count jobs by employer
    long countByPostedByUserId(Long userId);
}