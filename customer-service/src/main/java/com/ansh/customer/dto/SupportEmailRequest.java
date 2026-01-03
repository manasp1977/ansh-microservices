package com.ansh.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupportEmailRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must be less than 200 characters")
    private String subject;

    @NotBlank(message = "Message body is required")
    @Size(max = 5000, message = "Message must be less than 5000 characters")
    private String body;

    @Email(message = "Valid email is required")
    private String userEmail;

    private String userName;

    private String userId;

    public SupportEmailRequest() {}

    public SupportEmailRequest(String subject, String body, String userEmail, String userName, String userId) {
        this.subject = subject;
        this.body = body;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
