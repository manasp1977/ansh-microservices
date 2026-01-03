package com.ansh.analytics.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Overall platform analytics
 */
public class PlatformAnalyticsResponse {
    private Long totalListings;
    private Long totalSoldListings;
    private Long totalAvailableListings;
    private BigDecimal totalTransactionValue;
    private BigDecimal averageListingPrice;
    private BigDecimal totalSavings;
    private List<CategoryStats> categoryBreakdown;

    public PlatformAnalyticsResponse() {
    }

    public Long getTotalListings() {
        return totalListings;
    }

    public void setTotalListings(Long totalListings) {
        this.totalListings = totalListings;
    }

    public Long getTotalSoldListings() {
        return totalSoldListings;
    }

    public void setTotalSoldListings(Long totalSoldListings) {
        this.totalSoldListings = totalSoldListings;
    }

    public Long getTotalAvailableListings() {
        return totalAvailableListings;
    }

    public void setTotalAvailableListings(Long totalAvailableListings) {
        this.totalAvailableListings = totalAvailableListings;
    }

    public BigDecimal getTotalTransactionValue() {
        return totalTransactionValue;
    }

    public void setTotalTransactionValue(BigDecimal totalTransactionValue) {
        this.totalTransactionValue = totalTransactionValue;
    }

    public BigDecimal getAverageListingPrice() {
        return averageListingPrice;
    }

    public void setAverageListingPrice(BigDecimal averageListingPrice) {
        this.averageListingPrice = averageListingPrice;
    }

    public List<CategoryStats> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategoryStats> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }

    public static class CategoryStats {
        private String category;
        private Long count;
        private BigDecimal totalValue;

        public CategoryStats() {
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public BigDecimal getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(BigDecimal totalValue) {
            this.totalValue = totalValue;
        }
    }
}
