package com.ansh.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for settlement data from Settlement Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementDTO {
    private String id;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private String notes;
    private LocalDateTime settledDate;

    public BigDecimal getAmount() {
        return amount;
    }
}
