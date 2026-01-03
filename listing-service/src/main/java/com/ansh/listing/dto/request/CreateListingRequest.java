package com.ansh.listing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for creating a marketplace listing.
 */
public class CreateListingRequest {

    @NotBlank(message = "Receipt ID is required")
    private String receiptId;

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    private String description;
    private String imageUrl;

    // Optional: Reference to specific receipt item for expense tracking
    private Long receiptItemId;

    // Optional: Original price from receipt for expense split calculation
    private BigDecimal originalPrice;

    // Optional: Split percentage (0-100) - defaults to 50% if not specified
    private BigDecimal splitPercentage;

    public CreateListingRequest() {
    }

    public CreateListingRequest(String receiptId, String itemName, BigDecimal quantity, BigDecimal unitPrice, String description, String imageUrl) {
        this.receiptId = receiptId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.description = description;
        this.imageUrl = imageUrl;
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

    public BigDecimal getSplitPercentage() {
        return splitPercentage;
    }

    public void setSplitPercentage(BigDecimal splitPercentage) {
        this.splitPercentage = splitPercentage;
    }
}
