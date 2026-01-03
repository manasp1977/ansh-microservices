package com.ansh.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Analytics data for a specific user
 */
@Data
@NoArgsConstructor
public class UserAnalyticsResponse {
    private String userId;
    private Long totalListingsCreated;
    private Long totalListingsSold;
    private Long totalListingsAvailable;
    private BigDecimal totalRevenue;
    private BigDecimal averageSellingPrice;
    private BigDecimal totalOriginalValue;
    private BigDecimal totalSellingValue;
    private BigDecimal userSavings;

    public UserAnalyticsResponse(String userId, Long totalListingsCreated, Long totalListingsSold,
                                 Long totalListingsAvailable, BigDecimal totalRevenue,
                                 BigDecimal averageSellingPrice, BigDecimal totalOriginalValue,
                                 BigDecimal totalSellingValue, BigDecimal userSavings) {
        this.userId = userId;
        this.totalListingsCreated = totalListingsCreated;
        this.totalListingsSold = totalListingsSold;
        this.totalListingsAvailable = totalListingsAvailable;
        this.totalRevenue = totalRevenue;
        this.averageSellingPrice = averageSellingPrice;
        this.totalOriginalValue = totalOriginalValue;
        this.totalSellingValue = totalSellingValue;
        this.userSavings = userSavings;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getTotalListingsCreated() {
        return totalListingsCreated;
    }

    public void setTotalListingsCreated(Long totalListingsCreated) {
        this.totalListingsCreated = totalListingsCreated;
    }

    public Long getTotalListingsSold() {
        return totalListingsSold;
    }

    public void setTotalListingsSold(Long totalListingsSold) {
        this.totalListingsSold = totalListingsSold;
    }

    public Long getTotalListingsAvailable() {
        return totalListingsAvailable;
    }

    public void setTotalListingsAvailable(Long totalListingsAvailable) {
        this.totalListingsAvailable = totalListingsAvailable;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageSellingPrice() {
        return averageSellingPrice;
    }

    public void setAverageSellingPrice(BigDecimal averageSellingPrice) {
        this.averageSellingPrice = averageSellingPrice;
    }

    public BigDecimal getTotalOriginalValue() {
        return totalOriginalValue;
    }

    public void setTotalOriginalValue(BigDecimal totalOriginalValue) {
        this.totalOriginalValue = totalOriginalValue;
    }

    public BigDecimal getTotalSellingValue() {
        return totalSellingValue;
    }

    public void setTotalSellingValue(BigDecimal totalSellingValue) {
        this.totalSellingValue = totalSellingValue;
    }

    public BigDecimal getUserSavings() {
        return userSavings;
    }

    public void setUserSavings(BigDecimal userSavings) {
        this.userSavings = userSavings;
    }
}
