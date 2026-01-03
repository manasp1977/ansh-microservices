package com.ansh.listing.enums;

/**
 * Status enum for marketplace listings.
 */
public enum ListingStatus {
    /**
     * Listing is available for purchase
     */
    AVAILABLE("available"),

    /**
     * Listing has been sold/purchased
     */
    SOLD("sold"),

    /**
     * Listing was cancelled by the seller
     */
    CANCELLED("cancelled");

    private final String value;

    ListingStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Convert string value to enum
     */
    public static ListingStatus fromValue(String value) {
        for (ListingStatus status : ListingStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown listing status: " + value);
    }
}
