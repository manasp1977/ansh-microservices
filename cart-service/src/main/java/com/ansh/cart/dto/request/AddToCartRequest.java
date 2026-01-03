package com.ansh.cart.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for adding item to cart.
 */
public class AddToCartRequest {

    @NotBlank(message = "Listing ID is required")
    private String listingId;

    public AddToCartRequest() {
    }

    public AddToCartRequest(String listingId) {
        this.listingId = listingId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }
}
