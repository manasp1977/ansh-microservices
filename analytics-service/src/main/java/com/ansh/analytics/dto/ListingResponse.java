package com.ansh.analytics.dto;

import java.util.List;

/**
 * Response wrapper for listing service API calls.
 * The listing service returns listings wrapped in a response object.
 */
public class ListingResponse {
    private List<ListingDTO> listings;

    public ListingResponse() {
    }

    public List<ListingDTO> getListings() {
        return listings;
    }

    public void setListings(List<ListingDTO> listings) {
        this.listings = listings;
    }
}
