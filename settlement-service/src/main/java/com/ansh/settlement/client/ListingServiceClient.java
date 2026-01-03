package com.ansh.settlement.client;

import com.ansh.settlement.dto.ListingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Listing Service.
 * Used to fetch sold listings for balance calculation.
 */
@FeignClient(name = "LISTING-SERVICE")
public interface ListingServiceClient {

    /**
     * Get all sold listings for global balance calculation
     * Calls Listing Service endpoint: GET /listings/sold
     */
    @GetMapping("/listings/sold")
    ListingResponse getAllSoldListings();

    /**
     * Get sold listings by user for user-specific balance calculation
     * Calls Listing Service endpoint: GET /listings/sold/user/{userId}
     */
    @GetMapping("/listings/sold/user/{userId}")
    ListingResponse getSoldListingsByUser(@PathVariable("userId") String userId);
}
