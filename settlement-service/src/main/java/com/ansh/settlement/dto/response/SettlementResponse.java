package com.ansh.settlement.dto.response;

import com.ansh.settlement.entity.Settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for settlement response.
 * In microservices: User names are fetched separately via Auth Service if needed.
 */
public class SettlementResponse {
    private String id;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private String notes;
    private LocalDateTime settledDate;
    private LocalDateTime createdAt;

    public SettlementResponse() {
    }

    public SettlementResponse(String id, String payerId, String payeeId, BigDecimal amount, String notes, LocalDateTime settledDate, LocalDateTime createdAt) {
        this.id = id;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.notes = notes;
        this.settledDate = settledDate;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getSettledDate() {
        return settledDate;
    }

    public void setSettledDate(LocalDateTime settledDate) {
        this.settledDate = settledDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Convert Settlement entity to SettlementResponse DTO
     */
    public static SettlementResponse fromEntity(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getPayerId(),
                settlement.getPayeeId(),
                settlement.getAmount(),
                settlement.getNotes(),
                settlement.getSettledDate(),
                settlement.getCreatedAt()
        );
    }
}
