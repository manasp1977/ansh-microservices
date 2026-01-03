package com.ansh.admin.controller;

import com.ansh.admin.dto.response.PlatformStatsResponse;
import com.ansh.admin.dto.response.ReverseTransactionResponse;
import com.ansh.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for admin operations.
 * Note: API Gateway strips /api prefix, so routes here don't have /api
 * All endpoints should verify admin role (implemented at gateway level)
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * GET /admin/stats - Get platform-wide statistics
     * Accessed via API Gateway as: GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        PlatformStatsResponse stats = adminService.getPlatformStats();

        Map<String, Object> response = new HashMap<>();
        response.put("platform", "AnshShare");
        response.put("stats", stats);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /admin/reverse-transaction/{listingId} - Reverse a transaction
     * Accessed via API Gateway as: POST /api/admin/reverse-transaction/{listingId}
     */
    @PostMapping("/reverse-transaction/{listingId}")
    public ResponseEntity<ReverseTransactionResponse> reverseTransaction(
            @PathVariable String listingId) {

        ReverseTransactionResponse result = adminService.reverseTransaction(listingId);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /admin/listings/{listingId} - Delete a listing
     * Accessed via API Gateway as: DELETE /api/admin/listings/{listingId}
     */
    @DeleteMapping("/listings/{listingId}")
    public ResponseEntity<Map<String, Object>> deleteListing(@PathVariable String listingId) {
        adminService.deleteListing(listingId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Listing deleted successfully");
        response.put("listingId", listingId);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /admin/cart/{userId} - Clear user's cart
     * Accessed via API Gateway as: DELETE /api/admin/cart/{userId}
     */
    @DeleteMapping("/cart/{userId}")
    public ResponseEntity<Map<String, Object>> clearUserCart(@PathVariable String userId) {
        adminService.clearUserCart(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User cart cleared successfully");
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /admin/settlements/{settlementId} - Delete a settlement
     * Accessed via API Gateway as: DELETE /api/admin/settlements/{settlementId}
     */
    @DeleteMapping("/settlements/{settlementId}")
    public ResponseEntity<Map<String, Object>> deleteSettlement(@PathVariable String settlementId) {
        adminService.deleteSettlement(settlementId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Settlement deleted successfully");
        response.put("settlementId", settlementId);

        return ResponseEntity.ok(response);
    }
}
