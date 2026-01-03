package com.ansh.receipt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating a receipt.
 * Supports both file upload and manual entry.
 */
public class CreateReceiptRequest {

    @NotBlank(message = "Store location is required")
    private String storeLocation;

    @NotNull(message = "Total is required")
    private BigDecimal total;

    @NotNull(message = "Tax is required")
    private BigDecimal tax;

    @NotEmpty(message = "Items list cannot be empty")
    private List<ReceiptItemDTO> items;

    private String imageUrl;

    // Optional: For manual entry, allows specifying receipt date
    private LocalDateTime receiptDate;

    // Optional: Description/notes for the receipt
    private String description;

    public CreateReceiptRequest() {
    }

    public CreateReceiptRequest(String storeLocation, BigDecimal total, BigDecimal tax, List<ReceiptItemDTO> items, String imageUrl) {
        this.storeLocation = storeLocation;
        this.total = total;
        this.tax = tax;
        this.items = items;
        this.imageUrl = imageUrl;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
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

    public List<ReceiptItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItemDTO> items) {
        this.items = items;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDateTime receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class ReceiptItemDTO {
        @NotBlank(message = "Item name is required")
        private String name;

        @NotNull(message = "Quantity is required")
        private BigDecimal quantity;

        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;

        @NotNull(message = "Total price is required")
        private BigDecimal totalPrice;

        private String itemCode;

        public ReceiptItemDTO() {
        }

        public ReceiptItemDTO(String name, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice, String itemCode) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
            this.itemCode = itemCode;
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
    }
}
