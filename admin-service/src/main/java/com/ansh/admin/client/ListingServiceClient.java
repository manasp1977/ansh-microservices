package com.ansh.admin.client;

import com.ansh.admin.dto.ListingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for communicating with Listing Service
 */
@FeignClient(name = "LISTING-SERVICE")
public interface ListingServiceClient {

    @GetMapping("/listings")
    List<ListingDTO> getAllListings();

    @GetMapping("/listings/{id}")
    ListingDTO getListing(@PathVariable("id") String id);

    @DeleteMapping("/listings/{id}")
    void deleteListing(@PathVariable("id") String id);

    @PutMapping("/listings/{id}/status")
    void updateListingStatus(@PathVariable("id") String id, @RequestParam String status);
}
