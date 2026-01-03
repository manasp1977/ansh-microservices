package com.ansh.cart.repository;

import com.ansh.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity.
 * In microservices: Updated queries to use userId and listingId (String fields).
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all cart items for a user
     * Note: No JOIN FETCH since listing data comes from Listing Service via Feign
     */
    List<CartItem> findByUserId(String userId);

    /**
     * Find cart item by user and listing
     */
    Optional<CartItem> findByUserIdAndListingId(String userId, String listingId);

    /**
     * Check if user already has listing in cart
     */
    boolean existsByUserIdAndListingId(String userId, String listingId);

    /**
     * Delete all cart items for a user
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    /**
     * Delete specific cart item by user and listing
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId AND c.listingId = :listingId")
    void deleteByUserIdAndListingId(@Param("userId") String userId, @Param("listingId") String listingId);

    /**
     * Count cart items for a user
     */
    int countByUserId(String userId);
}
