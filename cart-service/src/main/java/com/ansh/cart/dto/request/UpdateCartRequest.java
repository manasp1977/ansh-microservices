package com.ansh.cart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for updating cart item quantity.
 */
public class UpdateCartRequest {

    @NotBlank(message = "Listing ID is required")
    private String listingId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    public UpdateCartRequest() {
    }

    public UpdateCartRequest(String listingId, Integer quantity) {
        this.listingId = listingId;
        this.quantity = quantity;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
