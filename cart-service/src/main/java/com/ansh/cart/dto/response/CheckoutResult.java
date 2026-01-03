package com.ansh.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for checkout result.
 */
public class CheckoutResult {
    private List<PurchasedItem> purchased;
    private List<FailedItem> failed;

    public CheckoutResult() {
    }

    public CheckoutResult(List<PurchasedItem> purchased, List<FailedItem> failed) {
        this.purchased = purchased;
        this.failed = failed;
    }

    public List<PurchasedItem> getPurchased() {
        return purchased;
    }

    public void setPurchased(List<PurchasedItem> purchased) {
        this.purchased = purchased;
    }

    public List<FailedItem> getFailed() {
        return failed;
    }

    public void setFailed(List<FailedItem> failed) {
        this.failed = failed;
    }

    public static class PurchasedItem {
        private String id;
        private String itemName;
        private BigDecimal totalPrice;

        public PurchasedItem() {
        }

        public PurchasedItem(String id, String itemName, BigDecimal totalPrice) {
            this.id = id;
            this.itemName = itemName;
            this.totalPrice = totalPrice;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }

    public static class FailedItem {
        private String listingId;
        private String error;

        public FailedItem() {
        }

        public FailedItem(String listingId, String error) {
            this.listingId = listingId;
            this.error = error;
        }

        public String getListingId() {
            return listingId;
        }

        public void setListingId(String listingId) {
            this.listingId = listingId;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
