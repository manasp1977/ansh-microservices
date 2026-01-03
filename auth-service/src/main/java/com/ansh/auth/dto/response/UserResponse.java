package com.ansh.auth.dto.response;

import com.ansh.auth.entity.User;

import java.time.LocalDateTime;

/**
 * DTO for user response.
 */
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private String avatar;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(String id, String name, String email, String zipCode, String avatar, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.zipCode = zipCode;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }

    public UserResponse(String id, String name, String email, String zipCode, Double latitude, Double longitude, String avatar, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getZipCode(),
                user.getLatitude(),
                user.getLongitude(),
                user.getAvatar(),
                user.getCreatedAt()
        );
    }
}
