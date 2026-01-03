package com.ansh.receipt.dto.response;

import com.ansh.receipt.entity.Receipt;
import com.ansh.receipt.entity.ReceiptItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for receipt response.
 * In microservices: userName is not included as we don't have access to User entity.
 * Frontend can fetch user details separately if needed.
 */
public class ReceiptResponse {
    private String id;
    private String userId;
    private String storeLocation;
    private LocalDateTime date;
    private BigDecimal total;
    private BigDecimal tax;
    private String imageUrl;
    private String description;
    private List<ReceiptItemDTO> items;
    private LocalDateTime createdAt;

    public ReceiptResponse() {
    }

    public ReceiptResponse(String id, String userId, String storeLocation, LocalDateTime date, BigDecimal total, BigDecimal tax, String imageUrl, String description, List<ReceiptItemDTO> items, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.storeLocation = storeLocation;
        this.date = date;
        this.total = total;
        this.tax = tax;
        this.imageUrl = imageUrl;
        this.description = description;
        this.items = items;
        this.createdAt = createdAt;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ReceiptItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItemDTO> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class ReceiptItemDTO {
        private Long id;
        private String name;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String itemCode;
        private Boolean isListed;
        private BigDecimal listedQuantity;
        private BigDecimal availableQuantity;
        private String listingId;

        public ReceiptItemDTO() {
        }

        public ReceiptItemDTO(Long id, String name, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice,
                              String itemCode, Boolean isListed, BigDecimal listedQuantity, BigDecimal availableQuantity, String listingId) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
            this.itemCode = itemCode;
            this.isListed = isListed;
            this.listedQuantity = listedQuantity;
            this.availableQuantity = availableQuantity;
            this.listingId = listingId;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public BigDecimal getAvailableQuantity() {
            return availableQuantity;
        }

        public void setAvailableQuantity(BigDecimal availableQuantity) {
            this.availableQuantity = availableQuantity;
        }

        public String getListingId() {
            return listingId;
        }

        public void setListingId(String listingId) {
            this.listingId = listingId;
        }

        public static ReceiptItemDTO fromEntity(ReceiptItem item) {
            return new ReceiptItemDTO(
                    item.getId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice(),
                    item.getItemCode(),
                    item.getIsListed(),
                    item.getListedQuantity(),
                    item.getAvailableQuantity(),
                    item.getListingId()
            );
        }
    }

    /**
     * Convert Receipt entity to ReceiptResponse DTO
     */
    public static ReceiptResponse fromEntity(Receipt receipt) {
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getUserId(),
                receipt.getStoreLocation(),
                receipt.getDate(),
                receipt.getTotal(),
                receipt.getTax(),
                receipt.getImageUrl(),
                receipt.getDescription(),
                receipt.getItems().stream()
                        .map(ReceiptItemDTO::fromEntity)
                        .collect(Collectors.toList()),
                receipt.getCreatedAt()
        );
    }
}
