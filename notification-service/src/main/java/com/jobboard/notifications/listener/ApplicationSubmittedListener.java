package com.jobboard.notifications.listener;

import com.jobboard.notifications.config.RabbitMQConfig;
import com.jobboard.notifications.dto.ApplicationSubmittedEvent;
import com.jobboard.notifications.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationSubmittedListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.APPLICATION_SUBMITTED_QUEUE)
    public void handleApplicationSubmittedEvent(ApplicationSubmittedEvent event) {
        log.info("Received Application Submitted event: {}", event);

        try {
            // Send confirmation email to applicant
            emailService.sendApplicationSubmittedConfirmation(
                    event.getApplicantEmail(),
                    event.getJobTitle(),
                    event.getCompanyName()
            );

            // Send notification email to employer
            emailService.sendApplicationReceivedNotification(
                    event.getEmployerEmail(),
                    event.getJobTitle(),
                    event.getApplicantUsername()
            );

            log.info("Successfully processed Application Submitted event for application: {}", event.getApplicationId());
        } catch (Exception e) {
            log.error("Error processing Application Submitted event: {}", e.getMessage(), e);
            throw e;
        }
    }
}