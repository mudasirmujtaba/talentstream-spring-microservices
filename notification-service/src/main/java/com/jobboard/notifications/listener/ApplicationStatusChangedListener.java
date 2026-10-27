package com.jobboard.notifications.listener;

import com.jobboard.notifications.config.RabbitMQConfig;
import com.jobboard.notifications.dto.ApplicationStatusChangedEvent;
import com.jobboard.notifications.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStatusChangedListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.APPLICATION_STATUS_CHANGED_QUEUE)
    public void handleApplicationStatusChangedEvent(ApplicationStatusChangedEvent event) {
        log.info("Received Application Status Changed event: {}", event);

        try {
            // Send status update email to applicant
            emailService.sendApplicationStatusChangedNotification(
                    event.getApplicantEmail(),
                    event.getJobTitle(),
                    event.getCompanyName(),
                    event.getOldStatus(),
                    event.getNewStatus()
            );

            log.info("Successfully processed Application Status Changed event for application: {}", event.getApplicationId());
        } catch (Exception e) {
            log.error("Error processing Application Status Changed event: {}", e.getMessage(), e);
            throw e;
        }
    }
}