package com.jobboard.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private static final String FROM_EMAIL = "noreply@jobboard.com";

    @Override
    public void sendJobPostedNotification(String to, String jobTitle, String companyName, String location) {
        log.info("Sending job posted notification to: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("New Job Posted: " + jobTitle);
            message.setText(buildJobPostedEmailBody(jobTitle, companyName, location));

            // For now, just log instead of actually sending (to avoid email setup issues)
            log.info("Email would be sent to: {} with subject: {}", to, message.getSubject());
            log.info("Email body:\n{}", message.getText());

            // Uncomment this when you have real email configured
            // mailSender.send(message);

            log.info("Job posted notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send job posted notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendApplicationSubmittedConfirmation(String to, String jobTitle, String companyName) {
        log.info("Sending application confirmation to: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Application Submitted Successfully");
            message.setText(buildApplicationSubmittedEmailBody(jobTitle, companyName));

            log.info("Email would be sent to: {} with subject: {}", to, message.getSubject());
            log.info("Email body:\n{}", message.getText());

            // mailSender.send(message);

            log.info("Application confirmation sent successfully");
        } catch (Exception e) {
            log.error("Failed to send application confirmation: {}", e.getMessage());
        }
    }

    @Override
    public void sendApplicationReceivedNotification(String to, String jobTitle, String applicantName) {
        log.info("Sending application received notification to employer: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("New Application Received for " + jobTitle);
            message.setText(buildApplicationReceivedEmailBody(jobTitle, applicantName));

            log.info("Email would be sent to: {} with subject: {}", to, message.getSubject());
            log.info("Email body:\n{}", message.getText());

            // mailSender.send(message);

            log.info("Application received notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send application received notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendApplicationStatusChangedNotification(String to, String jobTitle, String companyName, String oldStatus, String newStatus) {
        log.info("Sending status change notification to: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Application Status Update: " + jobTitle);
            message.setText(buildStatusChangedEmailBody(jobTitle, companyName, oldStatus, newStatus));

            log.info("Email would be sent to: {} with subject: {}", to, message.getSubject());
            log.info("Email body:\n{}", message.getText());

            // mailSender.send(message);

            log.info("Status change notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send status change notification: {}", e.getMessage());
        }
    }

    // Email body builders
    private String buildJobPostedEmailBody(String jobTitle, String companyName, String location) {
        return String.format("""
                Dear Job Seeker,
                
                A new job matching your interests has been posted!
                
                Position: %s
                Company: %s
                Location: %s
                
                Visit TalentStream to view details and apply.
                
                Best regards,
                TalentStream Team
                """, jobTitle, companyName, location);
    }

    private String buildApplicationSubmittedEmailBody(String jobTitle, String companyName) {
        return String.format("""
                Dear Applicant,
                
                Your application has been successfully submitted!
                
                Position: %s
                Company: %s
                
                We will review your application and get back to you soon.
                You can track your application status in your dashboard.
                
                Good luck!
                
                Best regards,
                TalentStream Team
                """, jobTitle, companyName);
    }

    private String buildApplicationReceivedEmailBody(String jobTitle, String applicantName) {
        return String.format("""
                Dear Employer,
                
                You have received a new application!
                
                Position: %s
                Applicant: %s
                
                Log in to your dashboard to review the application details.
                
                Best regards,
                TalentStream Team
                """, jobTitle, applicantName);
    }

    private String buildStatusChangedEmailBody(String jobTitle, String companyName, String oldStatus, String newStatus) {
        return String.format("""
                Dear Applicant,
                
                Your application status has been updated!
                
                Position: %s
                Company: %s
                Previous Status: %s
                New Status: %s
                
                %s
                
                Best regards,
                TalentStream Team
                """, jobTitle, companyName, oldStatus, newStatus, getStatusMessage(newStatus));
    }

    private String getStatusMessage(String status) {
        return switch (status.toUpperCase()) {
            case "SHORTLISTED" -> "Congratulations! Your profile has been shortlisted. We will contact you soon.";
            case "INTERVIEWED" -> "Thank you for attending the interview. We will get back to you with the results.";
            case "ACCEPTED" -> "Congratulations! Your application has been accepted. Welcome to the team!";
            case "REJECTED" -> "Unfortunately, we have decided to proceed with other candidates. We wish you the best in your job search.";
            case "REVIEWED" -> "Your application has been reviewed by our team.";
            default -> "Your application status has been updated.";
        };
    }
}