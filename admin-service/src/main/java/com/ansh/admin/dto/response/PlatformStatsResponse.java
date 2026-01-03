package com.ansh.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Platform-wide statistics response
 */
@Data
@NoArgsConstructor
public class PlatformStatsResponse {
    private Long totalUsers;
    private Long totalListings;
    private Long totalSoldListings;
    private Long totalSettlements;
    private BigDecimal totalTransactionValue;
    private BigDecimal totalSettlementValue;

    public PlatformStatsResponse(Long totalUsers, Long totalListings, Long totalSoldListings,
                                 Long totalSettlements, BigDecimal totalTransactionValue,
                                 BigDecimal totalSettlementValue) {
        this.totalUsers = totalUsers;
        this.totalListings = totalListings;
        this.totalSoldListings = totalSoldListings;
        this.totalSettlements = totalSettlements;
        this.totalTransactionValue = totalTransactionValue;
        this.totalSettlementValue = totalSettlementValue;
    }
}
