package com.ansh.customer.service;

import com.ansh.customer.dto.SupportEmailRequest;
import com.ansh.customer.dto.SupportEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomerSupportService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerSupportService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${customer.support.email:support@anshshare.com}")
    private String supportEmail;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    public SupportEmailResponse sendSupportEmail(SupportEmailRequest request) {
        String ticketId = "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        logger.info("Processing support request - Ticket: {}, From: {}, Subject: {}",
                ticketId, request.getUserEmail(), request.getSubject());

        if (mailEnabled && mailSender != null) {
            try {
                // Send email to support team
                SimpleMailMessage supportMessage = new SimpleMailMessage();
                supportMessage.setTo(supportEmail);
                supportMessage.setSubject("[" + ticketId + "] " + request.getSubject());
                supportMessage.setText(buildSupportEmailBody(request, ticketId));
                supportMessage.setReplyTo(request.getUserEmail());

                mailSender.send(supportMessage);

                // Send confirmation to user
                SimpleMailMessage confirmationMessage = new SimpleMailMessage();
                confirmationMessage.setTo(request.getUserEmail());
                confirmationMessage.setSubject("Support Request Received - " + ticketId);
                confirmationMessage.setText(buildConfirmationEmailBody(request, ticketId));
                confirmationMessage.setFrom(supportEmail);

                mailSender.send(confirmationMessage);

                logger.info("Support emails sent successfully for ticket: {}", ticketId);

                return new SupportEmailResponse(
                        ticketId,
                        "Your support request has been submitted successfully. You will receive a confirmation email shortly.",
                        "SENT"
                );

            } catch (Exception e) {
                logger.error("Failed to send email for ticket {}: {}", ticketId, e.getMessage());
                return new SupportEmailResponse(
                        ticketId,
                        "Your request has been logged but email delivery failed. Our team will still review your request.",
                        "LOGGED"
                );
            }
        } else {
            // Mail not configured - just log the request
            logger.info("Mail not enabled. Logging support request - Ticket: {}", ticketId);
            logger.info("Subject: {}", request.getSubject());
            logger.info("From: {} ({})", request.getUserName(), request.getUserEmail());
            logger.info("Body: {}", request.getBody());

            return new SupportEmailResponse(
                    ticketId,
                    "Your support request has been received and logged. Our team will review it shortly.",
                    "LOGGED"
            );
        }
    }

    private String buildSupportEmailBody(SupportEmailRequest request, String ticketId) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SUPPORT REQUEST ===\n\n");
        sb.append("Ticket ID: ").append(ticketId).append("\n");
        sb.append("User ID: ").append(request.getUserId() != null ? request.getUserId() : "N/A").append("\n");
        sb.append("User Name: ").append(request.getUserName() != null ? request.getUserName() : "N/A").append("\n");
        sb.append("User Email: ").append(request.getUserEmail()).append("\n");
        sb.append("Subject: ").append(request.getSubject()).append("\n\n");
        sb.append("=== MESSAGE ===\n\n");
        sb.append(request.getBody()).append("\n\n");
        sb.append("=== END OF REQUEST ===");
        return sb.toString();
    }

    private String buildConfirmationEmailBody(SupportEmailRequest request, String ticketId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(request.getUserName() != null ? request.getUserName() : "Customer").append(",\n\n");
        sb.append("Thank you for contacting AnshShare Support.\n\n");
        sb.append("We have received your support request and assigned it the following ticket number:\n\n");
        sb.append("Ticket ID: ").append(ticketId).append("\n\n");
        sb.append("Subject: ").append(request.getSubject()).append("\n\n");
        sb.append("Our support team will review your request and respond as soon as possible.\n\n");
        sb.append("Please keep this ticket ID for your reference.\n\n");
        sb.append("Best regards,\n");
        sb.append("AnshShare Support Team");
        return sb.toString();
    }
}
