package com.ansh.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user signup request.
 */
public class SignupRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(min = 5, max = 10, message = "Zip code must be between 5 and 10 characters")
    private String zipCode;

    private Double latitude;

    private Double longitude;

    public SignupRequest() {
    }

    public SignupRequest(String name, String email, String password, String zipCode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.zipCode = zipCode;
    }

    public SignupRequest(String name, String email, String password, String zipCode, Double latitude, Double longitude) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
