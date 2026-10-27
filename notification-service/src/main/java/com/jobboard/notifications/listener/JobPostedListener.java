package com.jobboard.notifications.listener;

import com.jobboard.notifications.config.RabbitMQConfig;
import com.jobboard.notifications.dto.JobPostedEvent;
import com.jobboard.notifications.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobPostedListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.JOB_POSTED_QUEUE)
    public void handleJobPostedEvent(JobPostedEvent event) {
        log.info("Received Job Posted event: {}", event);

        try {
            List<String> subscribedEmails = event.getJobSeekerEmails();

            if (subscribedEmails == null || subscribedEmails.isEmpty()) {
                log.info("No subscribed users to notify for category: {}", event.getCategory());
                log.info("Job: {} posted by {}", event.getTitle(), event.getPostedByUsername());
                return;
            }

            log.info("===========================================");
            log.info("NEW JOB POSTED - CATEGORY: {}", event.getCategory());
            log.info("===========================================");
            log.info("Job: {}", event.getTitle());
            log.info("Company: {}", event.getCompanyName());
            log.info("Location: {}", event.getLocation());
            log.info("Salary: {} - {}", event.getSalaryMin(), event.getSalaryMax());
            log.info("Posted By: {}", event.getPostedByUsername());
            log.info("Notifying {} subscribed users", subscribedEmails.size());
            log.info("===========================================");

            // Send email to each subscribed user
            int successCount = 0;
            int failureCount = 0;

            for (String email : subscribedEmails) {
                try {
                    emailService.sendJobPostedNotification(
                            email,
                            event.getTitle(),
                            event.getCompanyName(),
                            event.getLocation()
                    );
                    successCount++;
                    log.debug("✓ Notification sent to: {}", email);
                } catch (Exception e) {
                    failureCount++;
                    log.error("✗ Failed to send notification to {}: {}", email, e.getMessage());
                }
            }

            log.info("===========================================");
            log.info("NOTIFICATION SUMMARY");
            log.info("Total: {} | Success: {} | Failed: {}",
                    subscribedEmails.size(), successCount, failureCount);
            log.info("===========================================");

            log.info("Successfully processed Job Posted event for job: {}", event.getTitle());

        } catch (Exception e) {
            log.error("Error processing Job Posted event: {}", e.getMessage(), e);
            throw e;
        }
    }
}