package com.ansh.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for listing data from Listing Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingDTO {
    private String id;
    private String sellerId;
    private String buyerId;
    private String receiptId;
    private String itemName;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime listedDate;
    private LocalDateTime purchasedDate;

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
