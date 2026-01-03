package com.ansh.admin.service;

import com.ansh.admin.client.AuthServiceClient;
import com.ansh.admin.client.CartServiceClient;
import com.ansh.admin.client.ListingServiceClient;
import com.ansh.admin.client.SettlementServiceClient;
import com.ansh.admin.dto.ListingDTO;
import com.ansh.admin.dto.SettlementDTO;
import com.ansh.admin.dto.UserDTO;
import com.ansh.admin.dto.response.PlatformStatsResponse;
import com.ansh.admin.dto.response.ReverseTransactionResponse;
import com.ansh.common.exception.BadRequestException;
import com.ansh.common.exception.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for admin operations.
 * Orchestrates calls to multiple services for cross-service admin operations.
 */
@Service
public class AdminService {

    @Autowired
    private ListingServiceClient listingServiceClient;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private SettlementServiceClient settlementServiceClient;

    @Autowired
    private CartServiceClient cartServiceClient;

    /**
     * Get platform-wide statistics
     * Aggregates data from multiple services
     */
    public PlatformStatsResponse getPlatformStats() {
        // Fetch data from all services via Feign
        List<UserDTO> users = authServiceClient.getAllUsers();
        List<ListingDTO> listings = listingServiceClient.getAllListings();
        List<SettlementDTO> settlements = settlementServiceClient.getAllSettlements();

        long totalUsers = users.size();
        long totalListings = listings.size();

        long totalSoldListings = listings.stream()
                .filter(listing -> "SOLD".equals(listing.getStatus()))
                .count();

        BigDecimal totalTransactionValue = listings.stream()
                .filter(listing -> "SOLD".equals(listing.getStatus()))
                .map(ListingDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalSettlements = settlements.size();

        BigDecimal totalSettlementValue = settlements.stream()
                .map(SettlementDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PlatformStatsResponse(
                totalUsers,
                totalListings,
                totalSoldListings,
                totalSettlements,
                totalTransactionValue,
                totalSettlementValue
        );
    }

    /**
     * Reverse a transaction (mark listing as available again)
     * Orchestrates updates across multiple services
     */
    public ReverseTransactionResponse reverseTransaction(String listingId) {
        try {
            // Fetch listing to verify it exists and is sold
            ListingDTO listing = listingServiceClient.getListing(listingId);

            if (!"SOLD".equals(listing.getStatus())) {
                throw new BadRequestException("Cannot reverse transaction - listing is not sold");
            }

            // Update listing status back to AVAILABLE
            listingServiceClient.updateListingStatus(listingId, "AVAILABLE");

            return new ReverseTransactionResponse(
                    listingId,
                    "Transaction reversed successfully",
                    true,
                    "AVAILABLE"
            );

        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Listing", "id", listingId);
        }
    }

    /**
     * Delete a listing (admin override)
     */
    public void deleteListing(String listingId) {
        try {
            listingServiceClient.deleteListing(listingId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Listing", "id", listingId);
        }
    }

    /**
     * Clear a user's cart (admin override)
     */
    public void clearUserCart(String userId) {
        try {
            cartServiceClient.clearUserCart(userId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
    }

    /**
     * Delete a settlement (admin override)
     */
    public void deleteSettlement(String settlementId) {
        try {
            settlementServiceClient.deleteSettlement(settlementId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Settlement", "id", settlementId);
        }
    }
}
