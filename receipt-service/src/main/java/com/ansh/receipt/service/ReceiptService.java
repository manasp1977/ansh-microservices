package com.ansh.receipt.service;

import com.ansh.receipt.dto.request.CreateReceiptRequest;
import com.ansh.receipt.dto.response.ReceiptResponse;
import com.ansh.receipt.entity.Receipt;
import com.ansh.receipt.entity.ReceiptItem;
import com.ansh.receipt.repository.ReceiptRepository;
import com.ansh.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for receipt management operations.
 * In microservices: No UserRepository dependency - userId comes from gateway headers.
 */
@Service
public class ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

    /**
     * Get all receipts
     */
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getAllReceipts() {
        return receiptRepository.findAllWithItems().stream()
                .map(ReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get receipts by user ID
     */
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByUserId(String userId) {
        return receiptRepository.findByUserIdWithItems(userId).stream()
                .map(ReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get receipt by ID
     */
    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptById(String receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));
        return ReceiptResponse.fromEntity(receipt);
    }

    /**
     * Upload/create new receipt
     * Note: userId comes from X-User-Id header set by API Gateway after JWT validation
     * Supports manual entry with optional receipt date
     */
    @Transactional
    public ReceiptResponse createReceipt(String userId, CreateReceiptRequest request) {
        // Use provided receipt date or default to now
        LocalDateTime receiptDate = request.getReceiptDate() != null
                ? request.getReceiptDate()
                : LocalDateTime.now();

        // Create receipt
        Receipt receipt = new Receipt(
                "receipt_" + UUID.randomUUID().toString().substring(0, 8),
                userId,  // User ID from gateway header
                request.getStoreLocation(),
                receiptDate,
                request.getTotal(),
                request.getTax(),
                request.getImageUrl()
        );

        // Set optional description
        if (request.getDescription() != null) {
            receipt.setDescription(request.getDescription());
        }

        // Add receipt items
        for (CreateReceiptRequest.ReceiptItemDTO itemDTO : request.getItems()) {
            ReceiptItem item = new ReceiptItem(
                    itemDTO.getName(),
                    itemDTO.getQuantity(),
                    itemDTO.getUnitPrice(),
                    itemDTO.getTotalPrice(),
                    itemDTO.getItemCode()
            );
            receipt.addItem(item);
        }

        // Save receipt
        receipt = receiptRepository.save(receipt);

        return ReceiptResponse.fromEntity(receipt);
    }

    /**
     * Get receipt items for a specific receipt
     */
    @Transactional(readOnly = true)
    public Receipt getReceiptEntity(String receiptId) {
        return receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));
    }

    /**
     * Mark a receipt item as listed with specific quantity
     * Supports partial quantity listings (e.g., listing 0.5 of 1 item)
     */
    @Transactional
    public void markItemAsListed(String receiptId, Long itemId, String listingId, BigDecimal quantity, String userId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));

        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Receipt", "id", receiptId);
        }

        // Find the item
        ReceiptItem item = receipt.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptItem", "id", itemId.toString()));

        // Check available quantity
        if (!item.canList(quantity)) {
            throw new IllegalArgumentException("Not enough quantity available. Available: " + item.getAvailableQuantity());
        }

        // Add to listed quantity (supports multiple partial listings)
        item.addToListedQuantity(listingId, quantity);
        receiptRepository.save(receipt);
    }

    /**
     * Delete receipt
     */
    @Transactional
    public void deleteReceipt(String receiptId, String userId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));

        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Receipt", "id", receiptId);
        }

        receiptRepository.delete(receipt);
    }

    /**
     * Unmark a receipt item as listed (when listing is removed from marketplace)
     * Restores the item's availability for re-listing
     */
    @Transactional
    public void unmarkItemAsListed(String receiptId, Long itemId, String userId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));

        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Receipt", "id", receiptId);
        }

        // Find the item
        ReceiptItem item = receipt.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptItem", "id", itemId.toString()));

        // Reset listing status
        item.removeFromListing();
        receiptRepository.save(receipt);
    }

    /**
     * Admin unmark a receipt item as listed (bypasses ownership check)
     * Used by admin to unlist any item from marketplace
     */
    @Transactional
    public void adminUnmarkItemAsListed(String receiptId, Long itemId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));

        // Find the item
        ReceiptItem item = receipt.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptItem", "id", itemId.toString()));

        // Reset listing status
        item.removeFromListing();
        receiptRepository.save(receipt);
    }
}
