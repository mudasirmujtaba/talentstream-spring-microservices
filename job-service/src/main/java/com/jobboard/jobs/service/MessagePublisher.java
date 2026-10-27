package com.jobboard.jobs.service;

import com.jobboard.jobs.config.RabbitMQConfig;
import com.jobboard.jobs.dto.ApplicationStatusChangedEvent;
import com.jobboard.jobs.dto.ApplicationSubmittedEvent;
import com.jobboard.jobs.dto.JobPostedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishJobPostedEvent(JobPostedEvent event) {
        log.info("Publishing Job Posted event: {}", event);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.JOB_BOARD_EXCHANGE,
                    RabbitMQConfig.JOB_POSTED_ROUTING_KEY,
                    event
            );
            log.info("Job Posted event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish Job Posted event: {}", e.getMessage(), e);
        }
    }

    public void publishApplicationSubmittedEvent(ApplicationSubmittedEvent event) {
        log.info("Publishing Application Submitted event: {}", event);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.JOB_BOARD_EXCHANGE,
                    RabbitMQConfig.APPLICATION_SUBMITTED_ROUTING_KEY,
                    event
            );
            log.info("Application Submitted event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish Application Submitted event: {}", e.getMessage(), e);
        }
    }

    public void publishApplicationStatusChangedEvent(ApplicationStatusChangedEvent event) {
        log.info("Publishing Application Status Changed event: {}", event);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.JOB_BOARD_EXCHANGE,
                    RabbitMQConfig.APPLICATION_STATUS_CHANGED_ROUTING_KEY,
                    event
            );
            log.info("Application Status Changed event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish Application Status Changed event: {}", e.getMessage(), e);
        }
    }
}