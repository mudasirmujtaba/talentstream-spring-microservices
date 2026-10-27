package com.jobboard.notifications.service;

public interface EmailService {

    void sendJobPostedNotification(String to, String jobTitle, String companyName, String location);

    void sendApplicationSubmittedConfirmation(String to, String jobTitle, String companyName);

    void sendApplicationReceivedNotification(String to, String jobTitle, String applicantName);

    void sendApplicationStatusChangedNotification(String to, String jobTitle, String companyName, String oldStatus, String newStatus);
}