package com.ansh.listing.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client for fetching user data from auth-service
 */
@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public UserClient(RestTemplate restTemplate, @Value("${auth.service.url:http://localhost:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    /**
     * Get user location data
     */
    public UserLocationDTO getUserLocation(String userId) {
        try {
            String url = authServiceUrl + "/users/" + userId + "/location";
            return restTemplate.getForObject(url, UserLocationDTO.class);
        } catch (Exception e) {
            // Return null if user not found or service unavailable
            return null;
        }
    }

    /**
     * DTO for user location data
     */
    public static class UserLocationDTO {
        private String userId;
        private Double latitude;
        private Double longitude;
        private String zipCode;

        public UserLocationDTO() {
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
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

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
    }
}
