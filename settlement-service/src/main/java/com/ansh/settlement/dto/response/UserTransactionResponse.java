package com.ansh.settlement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for user transaction details with other users.
 */
public class UserTransactionResponse {
    private String userId;
    private String userName;
    private Map<String, OtherUserTransaction> transactionsWithOthers;

    public UserTransactionResponse() {
    }

    public UserTransactionResponse(String userId, String userName, Map<String, OtherUserTransaction> transactionsWithOthers) {
        this.userId = userId;
        this.userName = userName;
        this.transactionsWithOthers = transactionsWithOthers;
    }

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

    public Map<String, OtherUserTransaction> getTransactionsWithOthers() {
        return transactionsWithOthers;
    }

    public void setTransactionsWithOthers(Map<String, OtherUserTransaction> transactionsWithOthers) {
        this.transactionsWithOthers = transactionsWithOthers;
    }

    public static class OtherUserTransaction {
        private String otherUserName;
        private BigDecimal owes;
        private BigDecimal owed;
        private BigDecimal net;
        private List<TransactionInfo> transactions;
        private List<SettlementInfo> settlements;

        public OtherUserTransaction() {
        }

        public OtherUserTransaction(String otherUserName, BigDecimal owes, BigDecimal owed, BigDecimal net, List<TransactionInfo> transactions, List<SettlementInfo> settlements) {
            this.otherUserName = otherUserName;
            this.owes = owes;
            this.owed = owed;
            this.net = net;
            this.transactions = transactions;
            this.settlements = settlements;
        }

        public String getOtherUserName() {
            return otherUserName;
        }

        public void setOtherUserName(String otherUserName) {
            this.otherUserName = otherUserName;
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

        public List<TransactionInfo> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<TransactionInfo> transactions) {
            this.transactions = transactions;
        }

        public List<SettlementInfo> getSettlements() {
            return settlements;
        }

        public void setSettlements(List<SettlementInfo> settlements) {
            this.settlements = settlements;
        }
    }

    public static class TransactionInfo {
        private String itemName;
        private BigDecimal amount;
        private LocalDateTime date;
        private String type; // "sold" or "bought"

        public TransactionInfo() {
        }

        public TransactionInfo(String itemName, BigDecimal amount, LocalDateTime date, String type) {
            this.itemName = itemName;
            this.amount = amount;
            this.date = date;
            this.type = type;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class SettlementInfo {
        private BigDecimal amount;
        private LocalDateTime date;
        private String notes;
        private String type; // "paid" or "received"

        public SettlementInfo() {
        }

        public SettlementInfo(BigDecimal amount, LocalDateTime date, String notes, String type) {
            this.amount = amount;
            this.date = date;
            this.notes = notes;
            this.type = type;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
