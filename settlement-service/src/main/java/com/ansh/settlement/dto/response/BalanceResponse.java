package com.ansh.settlement.dto.response;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for balance response.
 */
public class BalanceResponse {
    private Map<String, UserBalance> balances;

    public BalanceResponse() {
    }

    public BalanceResponse(Map<String, UserBalance> balances) {
        this.balances = balances;
    }

    public Map<String, UserBalance> getBalances() {
        return balances;
    }

    public void setBalances(Map<String, UserBalance> balances) {
        this.balances = balances;
    }

    public static class UserBalance {
        private String userId;
        private String userName;
        private String userEmail;
        private BigDecimal owes;     // Money user owes to others
        private BigDecimal owed;     // Money others owe to user
        private BigDecimal net;      // Net balance (positive = owed to them)
        private BigDecimal sold;     // Total value sold
        private BigDecimal bought;   // Total value bought
        private BigDecimal paid;     // Total paid in settlements
        private BigDecimal received; // Total received in settlements

        public UserBalance() {
        }

        public UserBalance(String userId, String userName, String userEmail, BigDecimal owes, BigDecimal owed, BigDecimal net, BigDecimal sold, BigDecimal bought, BigDecimal paid, BigDecimal received) {
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
            this.owes = owes;
            this.owed = owed;
            this.net = net;
            this.sold = sold;
            this.bought = bought;
            this.paid = paid;
            this.received = received;
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public BigDecimal getOwes() {
            return owes;
        }

        public void setOwes(BigDecimal owes) {
            this.owes = owes;
        }

        public BigDecimal getOwed() {
            return owed;
        }

        public void setOwed(BigDecimal owed) {
            this.owed = owed;
        }

        public BigDecimal getNet() {
            return net;
        }

        public void setNet(BigDecimal net) {
            this.net = net;
        }

        public BigDecimal getSold() {
            return sold;
        }

        public void setSold(BigDecimal sold) {
            this.sold = sold;
        }

        public BigDecimal getBought() {
            return bought;
        }

        public void setBought(BigDecimal bought) {
            this.bought = bought;
        }

        public BigDecimal getPaid() {
            return paid;
        }

        public void setPaid(BigDecimal paid) {
            this.paid = paid;
        }

        public BigDecimal getReceived() {
            return received;
        }

        public void setReceived(BigDecimal received) {
            this.received = received;
        }
    }
}
