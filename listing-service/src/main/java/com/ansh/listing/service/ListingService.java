package com.ansh.listing.service;

import com.ansh.common.exception.BadRequestException;
import com.ansh.common.exception.ResourceNotFoundException;
import com.ansh.listing.client.ReceiptServiceClient;
import com.ansh.listing.client.UserClient;
import com.ansh.listing.dto.ReceiptDTO;
import com.ansh.listing.dto.request.CreateListingRequest;
import com.ansh.listing.dto.response.ListingResponse;
import com.ansh.listing.entity.Listing;
import com.ansh.listing.enums.ListingStatus;
import com.ansh.listing.repository.ListingRepository;
import com.ansh.listing.util.DistanceCalculator;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for marketplace listing operations.
 * In microservices: No UserRepository or ReceiptRepository dependencies.
 * Uses Feign client to validate receipt exists via REST call.
 */
@Service
public class ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ReceiptServiceClient receiptServiceClient;

    @Autowired
    private ImageSearchService imageSearchService;

    @Autowired
    private UserClient userClient;

    /**
     * Get all available listings
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getAvailableListings() {
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE).stream()
                .map(ListingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get available listings with distance filtering using PostGIS
     * @param userId Current user ID
     * @param maxDistanceMiles Maximum distance in miles (null for no filtering)
     * @return List of listings within the specified distance with distance calculated
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getAvailableListingsWithDistance(String userId, Double maxDistanceMiles) {
        // Get current user's location
        UserClient.UserLocationDTO userLocation = userClient.getUserLocation(userId);

        if (userLocation == null || userLocation.getLatitude() == null || userLocation.getLongitude() == null) {
            // If user has no location, return all listings without distance info
            return getAvailableListings();
        }

        // Get listings with distance calculated via PostGIS
        List<Object[]> results = listingRepository.findAvailableListingsWithDistance(
            userLocation.getLatitude(),
            userLocation.getLongitude(),
            maxDistanceMiles
        );

        List<ListingResponse> responses = new ArrayList<>();

        for (Object[] result : results) {
            // result[0] = listing_id (String), result[1] = distance_miles (Double)
            String listingId = (String) result[0];
            Double distanceMiles = result[1] != null ? ((Number) result[1]).doubleValue() : null;

            // Fetch the full listing entity
            Listing listing = listingRepository.findById(listingId).orElse(null);
            if (listing == null) {
                continue; // Skip if listing not found
            }

            ListingResponse response = ListingResponse.fromEntity(listing);
            if (distanceMiles != null) {
                response.setDistanceMiles(DistanceCalculator.roundDistance(distanceMiles));
            }
            responses.add(response);
        }

        return responses;
    }

    /**
     * Get all listings (any status)
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getAllListings() {
        return listingRepository.findAll().stream()
                .map(ListingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get listings by seller ID
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getListingsBySellerId(String sellerId) {
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream()
                .map(ListingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get listing by ID
     */
    @Transactional(readOnly = true)
    public ListingResponse getListingById(String listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));
        return ListingResponse.fromEntity(listing);
    }

    /**
     * Create new marketplace listing
     * Uses Feign client to validate receipt exists and get store info for image search
     */
    @Transactional
    public ListingResponse createListing(String sellerId, CreateListingRequest request) {
        // Validate receipt exists via Feign client call to Receipt Service
        ReceiptDTO receipt;
        try {
            receipt = receiptServiceClient.getReceipt(request.getReceiptId());

            // Verify seller owns the receipt
            if (!receipt.getUserId().equals(sellerId)) {
                throw new BadRequestException("You can only create listings for your own receipts");
            }
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Receipt", "id", request.getReceiptId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to validate receipt: " + e.getMessage());
        }

        // Auto-fetch image if not provided, using item name and store location for better results
        String imageUrl = request.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // Use store location from receipt for context-aware image search
            String storeName = receipt.getStoreLocation();
            imageUrl = imageSearchService.searchImageForItem(request.getItemName(), storeName, null);
        }

        // Fetch seller's location for distance-based filtering
        UserClient.UserLocationDTO sellerLocation = userClient.getUserLocation(sellerId);
        Double sellerLat = null;
        Double sellerLon = null;
        if (sellerLocation != null) {
            sellerLat = sellerLocation.getLatitude();
            sellerLon = sellerLocation.getLongitude();
        }

        // Create listing
        Listing listing = new Listing(
                "listing_" + UUID.randomUUID().toString().substring(0, 8),
                sellerId,
                request.getReceiptId(),
                request.getItemName(),
                request.getQuantity(),
                request.getUnitPrice(),
                null,
                request.getDescription(),
                imageUrl,
                ListingStatus.AVAILABLE,
                null,
                null,
                0,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Set seller location for distance calculations
        listing.setSellerLatitude(sellerLat);
        listing.setSellerLongitude(sellerLon);

        // Calculate total price
        listing.calculateTotalPrice();

        // Set expense split fields if provided
        if (request.getReceiptItemId() != null) {
            listing.setReceiptItemId(request.getReceiptItemId());
        }
        if (request.getOriginalPrice() != null) {
            listing.setOriginalPrice(request.getOriginalPrice());
            // Calculate expense split
            listing.calculateExpenseSplit();
        }
        if (request.getSplitPercentage() != null) {
            listing.setSplitPercentage(request.getSplitPercentage());
        }

        // Save listing
        listing = listingRepository.save(listing);

        // Mark receipt item as listed in Receipt Service
        if (request.getReceiptItemId() != null) {
            try {
                Map<String, Object> markRequest = new HashMap<>();
                markRequest.put("listingId", listing.getId());
                markRequest.put("quantity", request.getQuantity());

                receiptServiceClient.markItemAsListed(
                        request.getReceiptId(),
                        request.getReceiptItemId(),
                        markRequest,
                        sellerId
                );
                logger.info("Marked receipt item {} as listed with listing {}",
                        request.getReceiptItemId(), listing.getId());
            } catch (FeignException e) {
                // Log warning but don't fail the listing creation
                logger.warn("Failed to mark receipt item as listed: {}", e.getMessage());
            }
        }

        return ListingResponse.fromEntity(listing);
    }

    /**
     * Purchase a listing
     */
    @Transactional
    public ListingResponse purchaseListing(String listingId, String buyerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        // Validate listing is available
        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            throw new BadRequestException("Listing is not available");
        }

        // Validate buyer is not the seller
        if (listing.getSellerId().equals(buyerId)) {
            throw new BadRequestException("Cannot purchase your own listing");
        }

        // Mark as sold
        listing.markAsSold(buyerId);
        listing = listingRepository.save(listing);

        return ListingResponse.fromEntity(listing);
    }

    /**
     * Cancel/delete a listing
     */
    @Transactional
    public void cancelListing(String listingId, String userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        // Only seller can cancel
        if (!listing.getSellerId().equals(userId)) {
            throw new BadRequestException("Only the seller can cancel this listing");
        }

        // Cannot cancel sold listings
        if (listing.getStatus() == ListingStatus.SOLD) {
            throw new BadRequestException("Cannot cancel a sold listing");
        }

        // Mark as cancelled
        listing.markAsCancelled();
        listingRepository.save(listing);

        // Unmark receipt item as listed in Receipt Service
        if (listing.getReceiptItemId() != null) {
            try {
                receiptServiceClient.unmarkItemAsListed(
                        listing.getReceiptId(),
                        listing.getReceiptItemId(),
                        userId
                );
                logger.info("Unmarked receipt item {} from listing {}",
                        listing.getReceiptItemId(), listingId);
            } catch (FeignException e) {
                // Log warning but don't fail the cancellation
                logger.warn("Failed to unmark receipt item as listed: {}", e.getMessage());
            }
        }
    }

    /**
     * Track view on a listing
     */
    @Transactional
    public int trackView(String listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        listing.incrementViewCount();
        listing = listingRepository.save(listing);

        return listing.getViewCount();
    }

    /**
     * Track cart add on a listing
     */
    @Transactional
    public int trackCartAdd(String listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        listing.incrementCartAddCount();
        listing = listingRepository.save(listing);

        return listing.getCartAddCount();
    }

    /**
     * Reverse transaction (admin function)
     */
    @Transactional
    public ListingResponse reverseTransaction(String listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        if (listing.getStatus() != ListingStatus.SOLD) {
            throw new BadRequestException("Can only reverse sold listings");
        }

        listing.reverseTransaction();
        listing = listingRepository.save(listing);

        return ListingResponse.fromEntity(listing);
    }

    /**
     * Get sold listings by user ID (for settlement service)
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getSoldListingsByUserId(String userId) {
        return listingRepository.findSoldListingsByUserId(userId).stream()
                .map(ListingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all sold listings (for balance calculation service)
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getAllSoldListings() {
        return listingRepository.findAllSoldListings().stream()
                .map(ListingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update listing image URL
     * Only the seller can update their listing's image
     */
    @Transactional
    public ListingResponse updateListingImage(String listingId, String userId, String imageUrl) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        // Only seller can update
        if (!listing.getSellerId().equals(userId)) {
            throw new BadRequestException("Only the seller can update this listing's image");
        }

        // Cannot update sold or cancelled listings
        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            throw new BadRequestException("Cannot update image for " + listing.getStatus().name().toLowerCase() + " listings");
        }

        listing.setImageUrl(imageUrl);
        listing.setUpdatedAt(LocalDateTime.now());
        listing = listingRepository.save(listing);

        return ListingResponse.fromEntity(listing);
    }

    /**
     * Get image suggestions for an item name
     */
    public List<String> getImageSuggestions(String itemName, int count) {
        return imageSearchService.getImageSuggestions(itemName, count);
    }
}
