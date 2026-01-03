package com.ansh.cart.client;

import com.ansh.cart.dto.ListingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Listing Service.
 * Used to get listing details and purchase listings during checkout.
 *
 * Direct URL configuration bypasses Eureka service discovery.
 * For production with Eureka, remove url parameter.
 */
@FeignClient(name = "LISTING-SERVICE", url = "http://localhost:8083")
public interface ListingServiceClient {

    /**
     * Get listing by ID
     * Calls Listing Service endpoint: GET /listings/{id}
     */
    @GetMapping("/listings/{id}")
    ListingDTO getListing(@PathVariable("id") String id);

    /**
     * Purchase a listing
     * Calls Listing Service endpoint: POST /listings/{id}/purchase
     * Requires X-User-Id header to identify the buyer
     */
    @PostMapping("/listings/{id}/purchase")
    void purchaseListing(@PathVariable("id") String id, @RequestHeader("X-User-Id") String userId);

    /**
     * Track cart add analytics
     * Calls Listing Service endpoint: POST /listings/{id}/track-cart-add
     */
    @PostMapping("/listings/{id}/track-cart-add")
    void trackCartAdd(@PathVariable("id") String id);
}
