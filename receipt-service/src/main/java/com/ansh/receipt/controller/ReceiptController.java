package com.ansh.receipt.controller;

import com.ansh.receipt.dto.request.CreateReceiptRequest;
import com.ansh.receipt.dto.response.ReceiptResponse;
import com.ansh.receipt.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for receipt management endpoints.
 * Note: API Gateway strips /api prefix, so routes here are /receipts/*
 */
@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    @Autowired
    private ReceiptService receiptService;

    /**
     * GET /receipts - Get all receipts
     * Accessed via API Gateway as: GET /api/receipts
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReceipts() {
        List<ReceiptResponse> receipts = receiptService.getAllReceipts();

        Map<String, Object> response = new HashMap<>();
        response.put("receipts", receipts);
        response.put("count", receipts.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /receipts/my - Get current user's receipts
     * Accessed via API Gateway as: GET /api/receipts/my
     * NOTE: This must be defined BEFORE /{id} to avoid path variable matching "my"
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyReceipts(
            @RequestHeader("X-User-Id") String userId) {

        List<ReceiptResponse> receipts = receiptService.getReceiptsByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("receipts", receipts);
        response.put("count", receipts.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /receipts/user/{userId} - Get receipts by user ID
     * Accessed via API Gateway as: GET /api/receipts/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getReceiptsByUserId(@PathVariable String userId) {
        List<ReceiptResponse> receipts = receiptService.getReceiptsByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("receipts", receipts);
        response.put("count", receipts.size());
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /receipts - Upload/create new receipt
     * Accessed via API Gateway as: POST /api/receipts
     * Uses X-User-Id header set by API Gateway after JWT validation
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReceipt(
            @Valid @RequestBody CreateReceiptRequest request,
            @RequestHeader("X-User-Id") String userId) {

        ReceiptResponse receipt = receiptService.createReceipt(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Receipt uploaded successfully");
        response.put("receipt", Map.of(
                "id", receipt.getId(),
                "total", receipt.getTotal(),
                "items_count", receipt.getItems().size()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /receipts/{id} - Get receipt by ID
     * Accessed via API Gateway as: GET /api/receipts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReceiptResponse> getReceiptById(@PathVariable String id) {
        ReceiptResponse receipt = receiptService.getReceiptById(id);
        return ResponseEntity.ok(receipt);
    }

    /**
     * DELETE /receipts/{id} - Delete receipt
     * Accessed via API Gateway as: DELETE /api/receipts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReceipt(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        receiptService.deleteReceipt(id, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Receipt deleted successfully");
        response.put("receipt_id", id);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /receipts/{id}/items - Get receipt items for marketplace listing selection
     * Accessed via API Gateway as: GET /api/receipts/{id}/items
     * Returns items with their availability status for listing
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<Map<String, Object>> getReceiptItems(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        ReceiptResponse receipt = receiptService.getReceiptById(id);

        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not authorized to view this receipt");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("receiptId", receipt.getId());
        response.put("storeLocation", receipt.getStoreLocation());
        response.put("date", receipt.getDate());
        response.put("items", receipt.getItems());
        response.put("count", receipt.getItems().size());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /receipts/{id}/items/{itemId}/list - Mark an item as listed in marketplace
     * Accessed via API Gateway as: POST /api/receipts/{id}/items/{itemId}/list
     * Supports partial quantity listings (e.g., 0.5 of 1 item for splitting)
     */
    @PostMapping("/{id}/items/{itemId}/list")
    public ResponseEntity<Map<String, Object>> markItemAsListed(
            @PathVariable String id,
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Id") String userId) {

        String listingId = (String) request.get("listingId");
        Object quantityObj = request.get("quantity");

        if (listingId == null || quantityObj == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "listingId and quantity are required");
            return ResponseEntity.badRequest().body(error);
        }

        // Convert quantity to BigDecimal (supports both integer and decimal values)
        java.math.BigDecimal quantity;
        if (quantityObj instanceof Number) {
            quantity = new java.math.BigDecimal(quantityObj.toString());
        } else {
            quantity = new java.math.BigDecimal((String) quantityObj);
        }

        try {
            receiptService.markItemAsListed(id, itemId, listingId, quantity, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item marked as listed");
            response.put("receiptId", id);
            response.put("itemId", itemId);
            response.put("listingId", listingId);
            response.put("quantity", quantity);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * POST /receipts/{id}/items/{itemId}/unlist - Remove listing and restore item availability
     * Accessed via API Gateway as: POST /api/receipts/{id}/items/{itemId}/unlist
     * Called when a listing is cancelled/deleted from marketplace
     */
    @PostMapping("/{id}/items/{itemId}/unlist")
    public ResponseEntity<Map<String, Object>> unmarkItemAsListed(
            @PathVariable String id,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") String userId) {

        try {
            receiptService.unmarkItemAsListed(id, itemId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item listing removed");
            response.put("receiptId", id);
            response.put("itemId", itemId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * POST /receipts/{id}/items/{itemId}/admin-unlist - Admin remove listing (bypasses ownership check)
     * Accessed via API Gateway as: POST /api/receipts/{id}/items/{itemId}/admin-unlist
     * Called by admin to unlist any item from marketplace
     */
    @PostMapping("/{id}/items/{itemId}/admin-unlist")
    public ResponseEntity<Map<String, Object>> adminUnmarkItemAsListed(
            @PathVariable String id,
            @PathVariable Long itemId) {

        try {
            receiptService.adminUnmarkItemAsListed(id, itemId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item listing removed by admin");
            response.put("receiptId", id);
            response.put("itemId", itemId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
