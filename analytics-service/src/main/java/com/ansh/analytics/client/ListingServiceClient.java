package com.ansh.analytics.client;

import com.ansh.analytics.dto.ListingDTO;
import com.ansh.analytics.dto.ListingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communicating with Listing Service.
 * Used to fetch listing data for analytics.
 */
@FeignClient(name = "LISTING-SERVICE")
public interface ListingServiceClient {

    /**
     * Get all listings
     */
    @GetMapping("/listings")
    ListingResponse getAllListings();

    /**
     * Get listings by seller ID
     */
    @GetMapping("/listings/seller/{sellerId}")
    ListingResponse getListingsBySeller(@PathVariable("sellerId") String sellerId);

    /**
     * Get all sold listings
     */
    @GetMapping("/listings/sold")
    ListingResponse getAllSoldListings();
}
