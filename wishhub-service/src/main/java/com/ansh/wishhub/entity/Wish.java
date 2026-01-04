package com.ansh.wishhub.entity;

import com.ansh.wishhub.enums.WishStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishes")
@EntityListeners(AuditingEntityListener.class)
public class Wish {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WishStatus status = WishStatus.OPEN;

    @Column(name = "claimer_id", length = 50)
    private String claimerId;

    @Column(name = "claimed_date")
    private LocalDateTime claimedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Wish() {
    }

    public Wish(String id, String userId, String itemName, String description, BigDecimal quantity, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.itemName = itemName;
        this.description = description;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.status = WishStatus.OPEN;
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

    public void claim(String claimerId) {
        this.status = WishStatus.CLAIMED;
        this.claimerId = claimerId;
        this.claimedDate = LocalDateTime.now();
    }

    public void cancel() {
        this.status = WishStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wish)) return false;
        Wish wish = (Wish) o;
        return id != null && id.equals(wish.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Wish{" +
                "id='" + id + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", status=" + status +
                '}';
    }
}
