package com.ansh.cart.controller;

import com.ansh.cart.dto.request.AddToCartRequest;
import com.ansh.cart.dto.request.UpdateCartRequest;
import com.ansh.cart.dto.response.CartResponse;
import com.ansh.cart.dto.response.CheckoutResult;
import com.ansh.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for shopping cart endpoints.
 * Note: API Gateway strips /api prefix, so routes here are /cart/*
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * GET /cart - Get user's cart
     * Accessed via API Gateway as: GET /api/cart
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(new CartResponse(java.util.Collections.emptyList(),
                    java.math.BigDecimal.ZERO, 0));
        }
        CartResponse cart = cartService.getUserCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /cart/add - Add item to cart
     * Accessed via API Gateway as: POST /api/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int cartCount = cartService.addToCart(userId, request.getListingId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added to cart");
        response.put("cart_count", cartCount);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /cart/update - Update cart item quantity
     * Accessed via API Gateway as: POST /api/cart/update
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCart(
            @Valid @RequestBody UpdateCartRequest request,
            @RequestHeader("X-User-Id") String userId) {

        cartService.updateQuantity(userId, request.getListingId(), request.getQuantity());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cart updated");
        response.put("quantity", request.getQuantity());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /cart/remove - Remove item from cart
     * Accessed via API Gateway as: POST /api/cart/remove
     */
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") String userId) {

        int cartCount = cartService.removeFromCart(userId, request.get("listing_id"));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Removed from cart");
        response.put("cart_count", cartCount);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /cart/checkout - Checkout cart
     * Accessed via API Gateway as: POST /api/cart/checkout
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestHeader("X-User-Id") String userId) {
        CheckoutResult result = cartService.checkout(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("Purchased %d item(s)", result.getPurchased().size()));
        response.put("purchased", result.getPurchased());
        response.put("failed", result.getFailed());

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /cart/clear - Clear all items from cart
     * Accessed via API Gateway as: DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart(@RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cart cleared");
        response.put("cart_count", 0);

        return ResponseEntity.ok(response);
    }
}
