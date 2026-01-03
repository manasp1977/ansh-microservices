package com.ansh.settlement.dto;

import java.util.List;

/**
 * Response wrapper for listing service API calls.
 */
public class ListingResponse {
    private List<ListingDTO> listings;
    private Integer count;

    public ListingResponse() {
    }

    public List<ListingDTO> getListings() {
        return listings;
    }

    public void setListings(List<ListingDTO> listings) {
        this.listings = listings;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
