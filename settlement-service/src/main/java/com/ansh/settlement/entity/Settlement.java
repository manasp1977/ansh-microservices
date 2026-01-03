package com.ansh.settlement.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Settlement entity representing payment settlements between users.
 * In microservices: payer and payee are stored as IDs, not JPA relationships.
 */
@Entity
@Table(name = "settlements")
@EntityListeners(AuditingEntityListener.class)
public class Settlement {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "payer_id", nullable = false, length = 50)
    private String payerId;

    @Column(name = "payee_id", nullable = false, length = 50)
    private String payeeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "settled_date", nullable = false)
    private LocalDateTime settledDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    // Constructors
    public Settlement() {
    }

    public Settlement(String id, String payerId, String payeeId, BigDecimal amount, String notes,
                      LocalDateTime settledDate, LocalDateTime createdAt) {
        this.id = id;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.notes = notes;
        this.settledDate = settledDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Settlement)) return false;
        Settlement that = (Settlement) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Settlement{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", settledDate=" + settledDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}
