package com.ansh.analytics.service;

import com.ansh.analytics.client.ListingServiceClient;
import com.ansh.analytics.dto.ListingDTO;
import com.ansh.analytics.dto.response.PlatformAnalyticsResponse;
import com.ansh.analytics.dto.response.UserAnalyticsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for analytics and reporting.
 * In microservices: Uses Feign client to fetch listing data from Listing Service.
 */
@Service
public class AnalyticsService {

    @Autowired
    private ListingServiceClient listingServiceClient;

    /**
     * Get analytics for a specific user
     * Fetches user's listings via Feign client
     */
    public UserAnalyticsResponse getUserAnalytics(String userId) {
        // Fetch user's listings from Listing Service via Feign
        List<ListingDTO> userListings = listingServiceClient.getListingsBySeller(userId).getListings();

        long totalListingsCreated = userListings.size();

        List<ListingDTO> soldListings = userListings.stream()
                .filter(listing -> "SOLD".equals(listing.getStatus()))
                .collect(Collectors.toList());

        long totalListingsSold = soldListings.size();
        long totalListingsAvailable = totalListingsCreated - totalListingsSold;

        BigDecimal totalRevenue = soldListings.stream()
                .map(ListingDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageSellingPrice = totalListingsSold > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalListingsSold), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalOriginalValue = userListings.stream()
                .map(ListingDTO::getOriginalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSellingValue = userListings.stream()
                .map(ListingDTO::getSellingPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate user savings based on split price
        // For sold items where user is seller: savings = originalPrice / 2
        // For purchased items where user is buyer: savings = originalPrice / 2
        BigDecimal sellerSavings = soldListings.stream()
                .map(ListingDTO::getOriginalPrice)
                .filter(price -> price != null)
                .map(price -> price.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get all sold listings to find purchases by this user
        List<ListingDTO> allSoldListings = listingServiceClient.getAllSoldListings().getListings();
        BigDecimal buyerSavings = allSoldListings.stream()
                .filter(listing -> userId.equals(listing.getBuyerId()))
                .map(ListingDTO::getOriginalPrice)
                .filter(price -> price != null)
                .map(price -> price.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal userSavings = sellerSavings.add(buyerSavings);

        return new UserAnalyticsResponse(
                userId,
                totalListingsCreated,
                totalListingsSold,
                totalListingsAvailable,
                totalRevenue,
                averageSellingPrice,
                totalOriginalValue,
                totalSellingValue,
                userSavings
        );
    }

    /**
     * Get platform-wide analytics
     * Fetches all listings via Feign client
     */
    public PlatformAnalyticsResponse getPlatformAnalytics() {
        // Fetch all listings from Listing Service via Feign
        List<ListingDTO> allListings = listingServiceClient.getAllListings().getListings();

        long totalListings = allListings.size();

        List<ListingDTO> soldListings = allListings.stream()
                .filter(listing -> "SOLD".equals(listing.getStatus()))
                .collect(Collectors.toList());

        long totalSoldListings = soldListings.size();
        long totalAvailableListings = totalListings - totalSoldListings;

        BigDecimal totalTransactionValue = soldListings.stream()
                .map(ListingDTO::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageListingPrice = totalListings > 0
                ? allListings.stream()
                    .map(ListingDTO::getSellingPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(totalListings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Total savings for admin view = sum of all sold items' original prices
        BigDecimal totalSavings = soldListings.stream()
                .map(ListingDTO::getOriginalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Category breakdown would require additional data from listings
        // For now, return empty list
        List<PlatformAnalyticsResponse.CategoryStats> categoryBreakdown = List.of();

        PlatformAnalyticsResponse response = new PlatformAnalyticsResponse();
        response.setTotalListings(totalListings);
        response.setTotalSoldListings(totalSoldListings);
        response.setTotalAvailableListings(totalAvailableListings);
        response.setTotalTransactionValue(totalTransactionValue);
        response.setAverageListingPrice(averageListingPrice);
        response.setTotalSavings(totalSavings);
        response.setCategoryBreakdown(categoryBreakdown);

        return response;
    }

    /**
     * Get top sellers analytics
     * Fetches sold listings via Feign client and aggregates by seller
     */
    public List<UserAnalyticsResponse> getTopSellers(int limit) {
        // Fetch all sold listings from Listing Service via Feign
        List<ListingDTO> soldListings = listingServiceClient.getAllSoldListings().getListings();

        // Group by seller and calculate analytics
        return soldListings.stream()
                .collect(Collectors.groupingBy(ListingDTO::getSellerId))
                .entrySet().stream()
                .<UserAnalyticsResponse>map(entry -> {
                    String sellerId = entry.getKey();
                    List<ListingDTO> sellerListings = entry.getValue();

                    long totalSold = sellerListings.size();
                    BigDecimal totalRevenue = sellerListings.stream()
                            .map(ListingDTO::getTotalPrice)
                            .filter(price -> price != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgPrice = totalSold > 0
                            ? totalRevenue.divide(BigDecimal.valueOf(totalSold), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new UserAnalyticsResponse(
                            sellerId,
                            totalSold,
                            totalSold,
                            0L,
                            totalRevenue,
                            avgPrice,
                            BigDecimal.ZERO,
                            totalRevenue,
                            BigDecimal.ZERO
                    );
                })
                .sorted(Comparator.comparing(UserAnalyticsResponse::getTotalRevenue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
