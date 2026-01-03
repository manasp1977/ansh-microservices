package com.ansh.listing.controller;

import com.ansh.listing.dto.request.CreateListingRequest;
import com.ansh.listing.dto.response.ListingResponse;
import com.ansh.listing.service.FileStorageService;
import com.ansh.listing.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for marketplace listing endpoints.
 * Note: API Gateway strips /api prefix, so routes here are /listings/*
 */
@RestController
@RequestMapping("/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * GET /listings - Get all available listings with optional distance filtering
     * Accessed via API Gateway as: GET /api/listings?maxDistanceMiles=10
     *
     * @param maxDistanceMiles Optional maximum distance in miles (null returns all listings)
     * @param userId Current user ID from JWT token (optional - used for distance calculation)
     * @return Listings filtered by distance if parameters provided, otherwise all listings
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAvailableListings(
            @RequestParam(required = false) Double maxDistanceMiles,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        List<ListingResponse> listings;

        // If distance filtering is requested and user is authenticated, use distance-based query
        if (maxDistanceMiles != null && userId != null) {
            listings = listingService.getAvailableListingsWithDistance(userId, maxDistanceMiles);
        } else {
            // Otherwise return all available listings
            listings = listingService.getAvailableListings();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("listings", listings);
        response.put("count", listings.size());
        if (maxDistanceMiles != null) {
            response.put("maxDistanceMiles", maxDistanceMiles);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * GET /listings/all - Get all listings (any status)
     * Accessed via API Gateway as: GET /api/listings/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllListings() {
        List<ListingResponse> listings = listingService.getAllListings();

        Map<String, Object> response = new HashMap<>();
        response.put("listings", listings);
        response.put("count", listings.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /listings/seller/{sellerId} - Get user's listings
     * Accessed via API Gateway as: GET /api/listings/seller/{sellerId}
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Map<String, Object>> getUserListings(@PathVariable String sellerId) {
        List<ListingResponse> listings = listingService.getListingsBySellerId(sellerId);

        Map<String, Object> response = new HashMap<>();
        response.put("listings", listings);
        response.put("count", listings.size());
        response.put("sellerId", sellerId);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /listings/{id} - Get listing by ID
     * Accessed via API Gateway as: GET /api/listings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListingById(@PathVariable String id) {
        ListingResponse listing = listingService.getListingById(id);
        return ResponseEntity.ok(listing);
    }

    /**
     * POST /listings - Create new listing
     * Accessed via API Gateway as: POST /api/listings
     * Uses X-User-Id header set by API Gateway after JWT validation
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createListing(
            @Valid @RequestBody CreateListingRequest request,
            @RequestHeader("X-User-Id") String userId) {

        ListingResponse listing = listingService.createListing(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Listing created successfully");
        response.put("listing", Map.of(
                "id", listing.getId(),
                "item_name", listing.getItemName(),
                "total_price", listing.getTotalPrice()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /listings/{id}/purchase - Purchase a listing
     * Accessed via API Gateway as: POST /api/listings/{id}/purchase
     */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Map<String, Object>> purchaseListing(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        ListingResponse listing = listingService.purchaseListing(id, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Purchase successful");
        response.put("listing", Map.of(
                "id", listing.getId(),
                "item_name", listing.getItemName(),
                "total_price", listing.getTotalPrice(),
                "seller_id", listing.getSellerId(),
                "buyer_id", listing.getBuyerId()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /listings/{id} - Cancel/delete listing
     * Accessed via API Gateway as: DELETE /api/listings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteListing(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        listingService.cancelListing(id, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Listing removed successfully");
        response.put("listing_id", id);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /listings/{id}/track-view - Track view analytics
     * Accessed via API Gateway as: POST /api/listings/{id}/track-view
     */
    @PostMapping("/{id}/track-view")
    public ResponseEntity<Map<String, Object>> trackView(@PathVariable String id) {
        int viewCount = listingService.trackView(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "View tracked");
        response.put("view_count", viewCount);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /listings/{id}/track-cart-add - Track cart add analytics
     * Accessed via API Gateway as: POST /api/listings/{id}/track-cart-add
     */
    @PostMapping("/{id}/track-cart-add")
    public ResponseEntity<Map<String, Object>> trackCartAdd(@PathVariable String id) {
        int cartAddCount = listingService.trackCartAdd(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cart add tracked");
        response.put("cart_add_count", cartAddCount);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /listings/sold - Get all sold listings
     * For Settlement Service to calculate balances
     * Accessed via API Gateway as: GET /api/listings/sold
     */
    @GetMapping("/sold")
    public ResponseEntity<Map<String, Object>> getAllSoldListings() {
        List<ListingResponse> listings = listingService.getAllSoldListings();

        Map<String, Object> response = new HashMap<>();
        response.put("listings", listings);
        response.put("count", listings.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /listings/sold/user/{userId} - Get sold listings by user
     * For Settlement Service to calculate user balances
     * Accessed via API Gateway as: GET /api/listings/sold/user/{userId}
     */
    @GetMapping("/sold/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSoldListingsByUser(@PathVariable String userId) {
        List<ListingResponse> listings = listingService.getSoldListingsByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("listings", listings);
        response.put("count", listings.size());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /listings/{id}/reverse - Reverse transaction (admin function)
     * Accessed via API Gateway as: POST /api/listings/{id}/reverse
     */
    @PostMapping("/{id}/reverse")
    public ResponseEntity<Map<String, Object>> reverseTransaction(@PathVariable String id) {
        ListingResponse listing = listingService.reverseTransaction(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Transaction reversed successfully");
        response.put("listing", listing);

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /listings/{id}/image - Update listing image
     * Accessed via API Gateway as: PUT /api/listings/{id}/image
     * Allows seller to upload/change their listing's image
     */
    @PutMapping("/{id}/image")
    public ResponseEntity<Map<String, Object>> updateListingImage(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") String userId) {

        String imageUrl = request.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Image URL is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ListingResponse listing = listingService.updateListingImage(id, userId, imageUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Image updated successfully");
        response.put("listing", listing);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /listings/image-suggestions - Get image suggestions for an item
     * Accessed via API Gateway as: GET /api/listings/image-suggestions?itemName=...&count=...
     * Returns multiple image options for users to choose from
     */
    @GetMapping("/image-suggestions")
    public ResponseEntity<Map<String, Object>> getImageSuggestions(
            @RequestParam String itemName,
            @RequestParam(defaultValue = "5") int count) {

        List<String> suggestions = listingService.getImageSuggestions(itemName, Math.min(count, 10));

        Map<String, Object> response = new HashMap<>();
        response.put("itemName", itemName);
        response.put("suggestions", suggestions);
        response.put("count", suggestions.size());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /listings/{id}/upload-image - Upload image file for a listing
     * Accessed via API Gateway as: POST /api/listings/{id}/upload-image
     * Accepts multipart/form-data with 'image' file field
     * Only the seller can upload images for their listings
     */
    @PostMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadListingImage(
            @PathVariable String id,
            @RequestParam("image") MultipartFile file,
            @RequestHeader("X-User-Id") String userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Store the uploaded file
            String imageUrl = fileStorageService.storeImage(file, id);

            // Update the listing with the new image URL
            ListingResponse listing = listingService.updateListingImage(id, userId, imageUrl);

            response.put("message", "Image uploaded successfully");
            response.put("imageUrl", imageUrl);
            response.put("listing", listing);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            response.put("error", "Failed to store image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
