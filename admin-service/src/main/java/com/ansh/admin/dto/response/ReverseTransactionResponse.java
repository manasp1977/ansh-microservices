package com.ansh.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for transaction reversal operations
 */
@Data
@NoArgsConstructor
public class ReverseTransactionResponse {
    private String listingId;
    private String message;
    private boolean success;
    private String newStatus;

    public ReverseTransactionResponse(String listingId, String message, boolean success, String newStatus) {
        this.listingId = listingId;
        this.message = message;
        this.success = success;
        this.newStatus = newStatus;
    }
}
