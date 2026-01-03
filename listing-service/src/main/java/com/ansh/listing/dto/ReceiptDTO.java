package com.ansh.listing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Receipt data received from Receipt Service.
 * Only includes fields needed for validation.
 */
public class ReceiptDTO {
    private String id;
    private String userId;
    private String storeLocation;
    private LocalDateTime date;
    private BigDecimal total;
    private BigDecimal tax;
    private String imageUrl;
    private LocalDateTime createdAt;

    public ReceiptDTO() {
    }

    public ReceiptDTO(String id, String userId, String storeLocation, LocalDateTime date, BigDecimal total, BigDecimal tax, String imageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.storeLocation = storeLocation;
        this.date = date;
        this.total = total;
        this.tax = tax;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
