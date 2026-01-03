package com.ansh.listing.dto.response;

import com.ansh.listing.entity.Listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for listing response.
 * In microservices: sellerName and buyerName are NOT included as we don't have access to User entity.
 * Frontend can fetch user details separately if needed.
 */
public class ListingResponse {
    private String id;
    private String sellerId;
    private String receiptId;
    private String itemName;
    private BigDecimal quantity;
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

    // Expense split fields
    private Long receiptItemId;
    private BigDecimal originalPrice;
    private BigDecimal sellerExpense;
    private BigDecimal buyerExpense;
    private BigDecimal splitPercentage;

    // Distance from current user (in miles)
    private Double distanceMiles;

    public ListingResponse() {
    }

    public ListingResponse(String id, String sellerId, String receiptId, String itemName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice, String description, String imageUrl, String status, String buyerId, LocalDateTime purchasedDate, Integer viewCount, Integer cartAddCount, LocalDateTime lastViewed, LocalDateTime createdAt) {
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

    public Double getDistanceMiles() {
        return distanceMiles;
    }

    public void setDistanceMiles(Double distanceMiles) {
        this.distanceMiles = distanceMiles;
    }

    /**
     * Convert Listing entity to ListingResponse DTO
     */
    public static ListingResponse fromEntity(Listing listing) {
        ListingResponse response = new ListingResponse(
                listing.getId(),
                listing.getSellerId(),
                listing.getReceiptId(),
                listing.getItemName(),
                listing.getQuantity(),
                listing.getUnitPrice(),
                listing.getTotalPrice(),
                listing.getDescription(),
                listing.getImageUrl(),
                listing.getStatus().getValue(),
                listing.getBuyerId(),
                listing.getPurchasedDate(),
                listing.getViewCount(),
                listing.getCartAddCount(),
                listing.getLastViewed(),
                listing.getCreatedAt()
        );

        // Add expense split fields
        response.setReceiptItemId(listing.getReceiptItemId());
        response.setOriginalPrice(listing.getOriginalPrice());
        response.setSellerExpense(listing.getSellerExpense());
        response.setBuyerExpense(listing.getBuyerExpense());
        response.setSplitPercentage(listing.getSplitPercentage());

        return response;
    }
}
