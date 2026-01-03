package com.ansh.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Listing data received from Listing Service.
 */
public class ListingDTO {
    private String id;
    private String sellerId;
    private String receiptId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String description;
    private String imageUrl;
    private String status;
    private String buyerId;
    private LocalDateTime purchasedDate;
    private Integer viewCount;
    private Integer cartAddCount;
    private LocalDateTime lastViewed;
    private LocalDateTime createdAt;

    public ListingDTO() {
    }

    public ListingDTO(String id, String sellerId, String receiptId, String itemName, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice, String description, String imageUrl, String status, String buyerId, LocalDateTime purchasedDate, Integer viewCount, Integer cartAddCount, LocalDateTime lastViewed, LocalDateTime createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.receiptId = receiptId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.buyerId = buyerId;
        this.purchasedDate = purchasedDate;
        this.viewCount = viewCount;
        this.cartAddCount = cartAddCount;
        this.lastViewed = lastViewed;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public LocalDateTime getPurchasedDate() {
        return purchasedDate;
    }

    public void setPurchasedDate(LocalDateTime purchasedDate) {
        this.purchasedDate = purchasedDate;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getCartAddCount() {
        return cartAddCount;
    }

    public void setCartAddCount(Integer cartAddCount) {
        this.cartAddCount = cartAddCount;
    }

    public LocalDateTime getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(LocalDateTime lastViewed) {
        this.lastViewed = lastViewed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
