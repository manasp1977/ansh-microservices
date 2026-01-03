package com.ansh.receipt.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * ReceiptItem entity representing individual items from receipts.
 */
@Entity
@Table(name = "receipt_items")
public class ReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "item_code", length = 100)
    private String itemCode;

    @Column(name = "is_listed", nullable = false)
    private Boolean isListed = false;

    @Column(name = "listed_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal listedQuantity = BigDecimal.ZERO;

    @Column(name = "listing_id", length = 50)
    private String listingId;

    // Constructors
    public ReceiptItem() {
    }

    public ReceiptItem(String name, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice, String itemCode) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.itemCode = itemCode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Boolean getIsListed() {
        return isListed;
    }

    public void setIsListed(Boolean isListed) {
        this.isListed = isListed;
    }

    public BigDecimal getListedQuantity() {
        return listedQuantity;
    }

    public void setListedQuantity(BigDecimal listedQuantity) {
        this.listedQuantity = listedQuantity;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    /**
     * Get available quantity (original - listed)
     */
    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(listedQuantity);
    }

    /**
     * Add to listed quantity (supports multiple partial listings)
     */
    public void addToListedQuantity(String listingId, BigDecimal quantityToList) {
        this.listingId = listingId;
        this.listedQuantity = this.listedQuantity.add(quantityToList);
        this.isListed = true;
    }

    /**
     * Check if more quantity can be listed
     */
    public boolean canList(BigDecimal quantityToList) {
        return getAvailableQuantity().compareTo(quantityToList) >= 0;
    }

    /**
     * Remove from listing - reset listing status and restore availability
     */
    public void removeFromListing() {
        this.listingId = null;
        this.listedQuantity = BigDecimal.ZERO;
        this.isListed = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceiptItem)) return false;
        ReceiptItem that = (ReceiptItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ReceiptItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
