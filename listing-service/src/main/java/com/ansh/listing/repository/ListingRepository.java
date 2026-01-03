package com.ansh.listing.repository;

import com.ansh.listing.entity.Listing;
import com.ansh.listing.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Listing entity.
 * In microservices: Updated queries to use sellerId and buyerId (String fields).
 */
@Repository
public interface ListingRepository extends JpaRepository<Listing, String> {

    /**
     * Find all listings by status
     */
    List<Listing> findByStatus(ListingStatus status);

    /**
     * Find available listings ordered by creation date (newest first)
     */
    List<Listing> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    /**
     * Find listings by seller ID
     */
    List<Listing> findBySellerIdOrderByCreatedAtDesc(String sellerId);

    /**
     * Find listings by buyer ID
     */
    List<Listing> findByBuyerIdOrderByPurchasedDateDesc(String buyerId);

    /**
     * Find sold listings for balance calculation
     * Updated: Uses l.sellerId and l.buyerId instead of l.seller.id and l.buyer.id
     */
    @Query("SELECT l FROM Listing l WHERE l.status = 'SOLD' AND (l.sellerId = :userId OR l.buyerId = :userId)")
    List<Listing> findSoldListingsByUserId(@Param("userId") String userId);

    /**
     * Find all sold listings for global balance calculation
     */
    @Query("SELECT l FROM Listing l WHERE l.status = 'SOLD'")
    List<Listing> findAllSoldListings();

    /**
     * Find top listings by view count
     */
    @Query("SELECT l FROM Listing l WHERE l.status = 'AVAILABLE' ORDER BY l.viewCount DESC")
    List<Listing> findTopByViewCount();

    /**
     * Find top listings by cart add count
     */
    @Query("SELECT l FROM Listing l WHERE l.status = 'AVAILABLE' ORDER BY l.cartAddCount DESC")
    List<Listing> findTopByCartAddCount();

    /**
     * Count listings by seller and status
     */
    long countBySellerIdAndStatus(String sellerId, ListingStatus status);

    /**
     * Count total listings by seller
     */
    long countBySellerId(String sellerId);

    /**
     * Get sum of view counts for a seller
     * Updated: Uses l.sellerId instead of l.seller.id
     */
    @Query("SELECT COALESCE(SUM(l.viewCount), 0) FROM Listing l WHERE l.sellerId = :sellerId")
    Long sumViewCountBySellerId(@Param("sellerId") String sellerId);

    /**
     * Get sum of cart add counts for a seller
     * Updated: Uses l.sellerId instead of l.seller.id
     */
    @Query("SELECT COALESCE(SUM(l.cartAddCount), 0) FROM Listing l WHERE l.sellerId = :sellerId")
    Long sumCartAddCountBySellerId(@Param("sellerId") String sellerId);

    /**
     * Find listings by seller with specific status
     */
    List<Listing> findBySellerIdAndStatusOrderByCreatedAtDesc(String sellerId, ListingStatus status);

    /**
     * Find available listing IDs with distance calculation using PostGIS
     * Distance is calculated in meters and converted to miles (1 meter = 0.000621371 miles)
     * Results are sorted by distance (closest first)
     *
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @param maxDistanceMiles Maximum distance in miles (if null, no distance filter applied)
     * @return List of [listing_id, distance_in_miles] ordered by distance ascending
     */
    @Query(value = "SELECT l.id, " +
           "ST_Distance(" +
           "  CAST(ST_MakePoint(:userLon, :userLat) AS geography), " +
           "  CAST(ST_MakePoint(l.seller_longitude, l.seller_latitude) AS geography)" +
           ") * 0.000621371 AS distance_miles " +
           "FROM listings l " +
           "WHERE l.status = 'AVAILABLE' " +
           "AND l.seller_latitude IS NOT NULL " +
           "AND l.seller_longitude IS NOT NULL " +
           "AND (:maxDistanceMiles IS NULL OR " +
           "     ST_Distance(" +
           "       CAST(ST_MakePoint(:userLon, :userLat) AS geography), " +
           "       CAST(ST_MakePoint(l.seller_longitude, l.seller_latitude) AS geography)" +
           "     ) * 0.000621371 <= :maxDistanceMiles) " +
           "ORDER BY distance_miles ASC",
           nativeQuery = true)
    List<Object[]> findAvailableListingsWithDistance(
        @Param("userLat") Double userLat,
        @Param("userLon") Double userLon,
        @Param("maxDistanceMiles") Double maxDistanceMiles
    );
}
