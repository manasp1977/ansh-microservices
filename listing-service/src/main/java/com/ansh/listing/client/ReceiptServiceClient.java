package com.ansh.listing.client;

import com.ansh.listing.dto.ReceiptDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * Feign client for Receipt Service.
 * Used to validate that receipts exist before creating listings
 * and to mark receipt items as listed.
 *
 * Uses Eureka service discovery to locate RECEIPT-SERVICE.
 */
@FeignClient(name = "RECEIPT-SERVICE")
public interface ReceiptServiceClient {

    /**
     * Get receipt by ID to validate it exists
     * Calls Receipt Service endpoint: GET /receipts/{id}
     */
    @GetMapping("/receipts/{id}")
    ReceiptDTO getReceipt(@PathVariable("id") String id);

    /**
     * Mark a receipt item as listed in marketplace
     * Calls Receipt Service endpoint: POST /receipts/{id}/items/{itemId}/list
     */
    @PostMapping("/receipts/{receiptId}/items/{itemId}/list")
    Map<String, Object> markItemAsListed(
            @PathVariable("receiptId") String receiptId,
            @PathVariable("itemId") Long itemId,
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Id") String userId);

    /**
     * Unmark a receipt item as listed (when listing is cancelled/deleted)
     * Calls Receipt Service endpoint: POST /receipts/{id}/items/{itemId}/unlist
     */
    @PostMapping("/receipts/{receiptId}/items/{itemId}/unlist")
    Map<String, Object> unmarkItemAsListed(
            @PathVariable("receiptId") String receiptId,
            @PathVariable("itemId") Long itemId,
            @RequestHeader("X-User-Id") String userId);
}
