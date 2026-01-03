package com.ansh.cart.service;

import com.ansh.cart.client.ListingServiceClient;
import com.ansh.cart.dto.ListingDTO;
import com.ansh.cart.dto.response.CartResponse;
import com.ansh.cart.dto.response.CheckoutResult;
import com.ansh.cart.entity.CartItem;
import com.ansh.cart.repository.CartItemRepository;
import com.ansh.common.exception.BadRequestException;
import com.ansh.common.exception.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for shopping cart operations.
 * In microservices: Uses Feign client to fetch listing details and purchase listings.
 */
@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ListingServiceClient listingServiceClient;

    /**
     * Get user's cart
     */
    @Transactional(readOnly = true)
    public CartResponse getUserCart(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        List<CartResponse.CartItemDTO> cartItemDTOs = cartItems.stream()
                .map(cartItem -> {
                    // Fetch listing details via Feign client
                    try {
                        ListingDTO listing = listingServiceClient.getListing(cartItem.getListingId());

                        BigDecimal itemTotal = listing.getUnitPrice()
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

                        return new CartResponse.CartItemDTO(
                                listing.getId(),
                                listing.getItemName(),
                                listing.getQuantity(),
                                cartItem.getQuantity(),
                                listing.getUnitPrice(),
                                itemTotal,
                                listing.getImageUrl()
                        );
                    } catch (FeignException.NotFound e) {
                        // Listing was deleted, skip it
                        return null;
                    }
                })
                .filter(dto -> dto != null) // Remove null entries (deleted listings)
                .collect(Collectors.toList());

        BigDecimal total = cartItemDTOs.stream()
                .map(CartResponse.CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cartItemDTOs,
                total,
                cartItemDTOs.size()
        );
    }

    /**
     * Add item to cart
     */
    @Transactional
    public int addToCart(String userId, String listingId) {
        // Fetch listing via Feign client to validate it exists and is available
        ListingDTO listing;
        try {
            listing = listingServiceClient.getListing(listingId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Listing", "id", listingId);
        }

        // Validate listing is available
        if (!"available".equalsIgnoreCase(listing.getStatus())) {
            throw new BadRequestException("Listing is not available");
        }

        // Cannot add own listing to cart
        if (listing.getSellerId().equals(userId)) {
            throw new BadRequestException("Cannot add your own listing to cart");
        }

        // Check if already in cart
        if (cartItemRepository.existsByUserIdAndListingId(userId, listingId)) {
            throw new BadRequestException("Item already in cart");
        }

        // Add to cart
        CartItem cartItem = new CartItem(
                null,
                userId,
                listingId,
                1,
                null
        );

        cartItemRepository.save(cartItem);

        // Track analytics via Feign client
        try {
            listingServiceClient.trackCartAdd(listingId);
        } catch (Exception e) {
            // Log but don't fail if analytics tracking fails
        }

        return cartItemRepository.countByUserId(userId);
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public void updateQuantity(String userId, String listingId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndListingId(userId, listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Fetch listing to validate quantity doesn't exceed available
        try {
            ListingDTO listing = listingServiceClient.getListing(listingId);

            if (quantity > listing.getQuantity()) {
                throw new BadRequestException("Quantity exceeds available amount");
            }

            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);

        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Listing", "id", listingId);
        }
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public int removeFromCart(String userId, String listingId) {
        cartItemRepository.deleteByUserIdAndListingId(userId, listingId);
        return cartItemRepository.countByUserId(userId);
    }

    /**
     * Clear all items from cart
     */
    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    /**
     * Checkout - purchase all cart items
     */
    @Transactional
    public CheckoutResult checkout(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        List<CheckoutResult.PurchasedItem> purchased = new ArrayList<>();
        List<CheckoutResult.FailedItem> failed = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            try {
                // Fetch listing details
                ListingDTO listing = listingServiceClient.getListing(cartItem.getListingId());

                // Attempt purchase via Feign client with user ID header
                listingServiceClient.purchaseListing(listing.getId(), userId);

                purchased.add(new CheckoutResult.PurchasedItem(
                        listing.getId(),
                        listing.getItemName(),
                        listing.getTotalPrice()
                ));

            } catch (Exception e) {
                failed.add(new CheckoutResult.FailedItem(
                        cartItem.getListingId(),
                        e.getMessage()
                ));
            }
        }

        // Clear cart after checkout
        cartItemRepository.deleteByUserId(userId);

        return new CheckoutResult(
                purchased,
                failed
        );
    }
}
