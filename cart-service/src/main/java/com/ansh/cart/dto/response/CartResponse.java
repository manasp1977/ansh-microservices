package com.ansh.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for cart response.
 * In microservices: sellerName is NOT included (no access to User entity).
 */
public class CartResponse {
    private List<CartItemDTO> cart;
    private BigDecimal total;
    private Integer count;

    public CartResponse() {
    }

    public CartResponse(List<CartItemDTO> cart, BigDecimal total, Integer count) {
        this.cart = cart;
        this.total = total;
        this.count = count;
    }

    public List<CartItemDTO> getCart() {
        return cart;
    }

    public void setCart(List<CartItemDTO> cart) {
        this.cart = cart;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public static class CartItemDTO {
        private String listingId;
        private String itemName;
        private Integer maxQuantity;
        private Integer cartQuantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String imageUrl;

        public CartItemDTO() {
        }

        public CartItemDTO(String listingId, String itemName, Integer maxQuantity, Integer cartQuantity, BigDecimal unitPrice, BigDecimal totalPrice, String imageUrl) {
            this.listingId = listingId;
            this.itemName = itemName;
            this.maxQuantity = maxQuantity;
            this.cartQuantity = cartQuantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
            this.imageUrl = imageUrl;
        }

        public String getListingId() {
            return listingId;
        }

        public void setListingId(String listingId) {
            this.listingId = listingId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public Integer getMaxQuantity() {
            return maxQuantity;
        }

        public void setMaxQuantity(Integer maxQuantity) {
            this.maxQuantity = maxQuantity;
        }

        public Integer getCartQuantity() {
            return cartQuantity;
        }

        public void setCartQuantity(Integer cartQuantity) {
            this.cartQuantity = cartQuantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
