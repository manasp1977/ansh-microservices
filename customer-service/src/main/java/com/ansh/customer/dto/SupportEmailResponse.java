package com.ansh.customer.dto;

import java.time.LocalDateTime;

public class SupportEmailResponse {

    private String ticketId;
    private String message;
    private String status;
    private LocalDateTime submittedAt;

    public SupportEmailResponse() {}

    public SupportEmailResponse(String ticketId, String message, String status) {
        this.ticketId = ticketId;
        this.message = message;
        this.status = status;
        this.submittedAt = LocalDateTime.now();
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
