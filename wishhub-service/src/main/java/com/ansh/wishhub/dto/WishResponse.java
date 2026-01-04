package com.ansh.wishhub.dto;

import com.ansh.wishhub.entity.Wish;
import com.ansh.wishhub.enums.WishStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishResponse {

    private String id;
    private String userId;
    private String userName;
    private String itemName;
    private String description;
    private BigDecimal quantity;
    private String imageUrl;
    private WishStatus status;
    private String claimerId;
    private String claimerName;
    private LocalDateTime claimedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WishResponse() {
    }

    public static WishResponse fromEntity(Wish wish) {
        WishResponse response = new WishResponse();
        response.setId(wish.getId());
        response.setUserId(wish.getUserId());
        response.setItemName(wish.getItemName());
        response.setDescription(wish.getDescription());
        response.setQuantity(wish.getQuantity());
        response.setImageUrl(wish.getImageUrl());
        response.setStatus(wish.getStatus());
        response.setClaimerId(wish.getClaimerId());
        response.setClaimedDate(wish.getClaimedDate());
        response.setCreatedAt(wish.getCreatedAt());
        response.setUpdatedAt(wish.getUpdatedAt());
        return response;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public WishStatus getStatus() {
        return status;
    }

    public void setStatus(WishStatus status) {
        this.status = status;
    }

    public String getClaimerId() {
        return claimerId;
    }

    public void setClaimerId(String claimerId) {
        this.claimerId = claimerId;
    }

    public String getClaimerName() {
        return claimerName;
    }

    public void setClaimerName(String claimerName) {
        this.claimerName = claimerName;
    }

    public LocalDateTime getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(LocalDateTime claimedDate) {
        this.claimedDate = claimedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
