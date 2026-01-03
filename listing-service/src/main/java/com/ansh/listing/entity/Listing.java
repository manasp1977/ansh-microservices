package com.ansh.listing.entity;

import com.ansh.listing.enums.ListingStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Listing entity representing marketplace listings for items users want to share/sell.
 * In microservices: seller, buyer, and receipt are stored as IDs, not JPA relationships.
 */
@Entity
@Table(name = "listings")
@EntityListeners(AuditingEntityListener.class)
public class Listing {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "seller_id", nullable = false, length = 50)
    private String sellerId;

    @Column(name = "receipt_id", nullable = false, length = 50)
    private String receiptId;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus status = ListingStatus.AVAILABLE;

    @Column(name = "buyer_id", length = 50)
    private String buyerId;

    @Column(name = "purchased_date")
    private LocalDateTime purchasedDate;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "cart_add_count", nullable = false)
    private Integer cartAddCount = 0;

    @Column(name = "last_viewed")
    private LocalDateTime lastViewed;

    // Expense split fields - for splitting costs between seller and buyer
    @Column(name = "receipt_item_id")
    private Long receiptItemId;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "seller_expense", precision = 10, scale = 2)
    private BigDecimal sellerExpense;

    @Column(name = "buyer_expense", precision = 10, scale = 2)
    private BigDecimal buyerExpense;

    @Column(name = "split_percentage", precision = 5, scale = 2)
    private BigDecimal splitPercentage;

    // Seller location for distance calculations (denormalized from User service)
    @Column(name = "seller_latitude")
    private Double sellerLatitude;

    @Column(name = "seller_longitude")
    private Double sellerLongitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Listing() {
    }

    public Listing(String id, String sellerId, String receiptId, String itemName, BigDecimal quantity,
                   BigDecimal unitPrice, BigDecimal totalPrice, String description, String imageUrl,
                   ListingStatus status, String buyerId, LocalDateTime purchasedDate, Integer viewCount,
                   Integer cartAddCount, LocalDateTime lastViewed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.receiptId = receiptId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status != null ? status : ListingStatus.AVAILABLE;
        this.buyerId = buyerId;
        this.purchasedDate = purchasedDate;
        this.viewCount = viewCount != null ? viewCount : 0;
        this.cartAddCount = cartAddCount != null ? cartAddCount : 0;
        this.lastViewed = lastViewed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
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

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
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

    public Long getReceiptItemId() {
        return receiptItemId;
    }

    public void setReceiptItemId(Long receiptItemId) {
        this.receiptItemId = receiptItemId;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getSellerExpense() {
        return sellerExpense;
    }

    public void setSellerExpense(BigDecimal sellerExpense) {
        this.sellerExpense = sellerExpense;
    }

    public BigDecimal getBuyerExpense() {
        return buyerExpense;
    }

    public void setBuyerExpense(BigDecimal buyerExpense) {
        this.buyerExpense = buyerExpense;
    }

    public BigDecimal getSplitPercentage() {
        return splitPercentage;
    }

    public void setSplitPercentage(BigDecimal splitPercentage) {
        this.splitPercentage = splitPercentage;
    }

    public Double getSellerLatitude() {
        return sellerLatitude;
    }

    public void setSellerLatitude(Double sellerLatitude) {
        this.sellerLatitude = sellerLatitude;
    }

    public Double getSellerLongitude() {
        return sellerLongitude;
    }

    public void setSellerLongitude(Double sellerLongitude) {
        this.sellerLongitude = sellerLongitude;
    }

    /**
     * Calculate expense split based on listing price and original receipt price
     * Buyer pays the listing price, seller's expense is the original cost minus buyer's portion
     */
    public void calculateExpenseSplit() {
        if (originalPrice != null && totalPrice != null) {
            // Buyer pays the listing price
            this.buyerExpense = totalPrice;
            // Seller's expense is original price minus what buyer pays
            this.sellerExpense = originalPrice.subtract(totalPrice);
            // Split percentage is buyer's share of original price
            if (originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                this.splitPercentage = totalPrice.multiply(BigDecimal.valueOf(100))
                        .divide(originalPrice, 2, java.math.RoundingMode.HALF_UP);
            }
        }
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

    /**
     * Calculate total price from quantity and unit price
     */
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(quantity);
        }
    }

    /**
     * Mark listing as sold
     */
    public void markAsSold(String buyerId) {
        this.status = ListingStatus.SOLD;
        this.buyerId = buyerId;
        this.purchasedDate = LocalDateTime.now();
    }

    /**
     * Mark listing as cancelled
     */
    public void markAsCancelled() {
        this.status = ListingStatus.CANCELLED;
    }

    /**
     * Reverse transaction (admin function)
     */
    public void reverseTransaction() {
        this.status = ListingStatus.AVAILABLE;
        this.buyerId = null;
        this.purchasedDate = null;
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount++;
        this.lastViewed = LocalDateTime.now();
    }

    /**
     * Increment cart add count
     */
    public void incrementCartAddCount() {
        this.cartAddCount++;
    }

    /**
     * Calculate conversion rate (cart adds / views)
     */
    public double getConversionRate() {
        if (viewCount == 0) return 0.0;
        return (double) cartAddCount / viewCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Listing)) return false;
        Listing listing = (Listing) o;
        return id != null && id.equals(listing.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Listing{" +
                "id='" + id + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                '}';
    }
}
