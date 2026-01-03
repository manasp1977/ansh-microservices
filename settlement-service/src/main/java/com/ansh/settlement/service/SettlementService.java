package com.ansh.settlement.service;

import com.ansh.common.exception.BadRequestException;
import com.ansh.common.exception.ResourceNotFoundException;
import com.ansh.settlement.client.AuthServiceClient;
import com.ansh.settlement.dto.UserDTO;
import com.ansh.settlement.dto.request.CreateSettlementRequest;
import com.ansh.settlement.dto.response.SettlementResponse;
import com.ansh.settlement.entity.Settlement;
import com.ansh.settlement.repository.SettlementRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for settlement operations.
 * In microservices: Uses Feign client to validate users exist.
 */
@Service
public class SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    /**
     * Get all settlements
     */
    @Transactional(readOnly = true)
    public List<SettlementResponse> getAllSettlements() {
        return settlementRepository.findAll().stream()
                .map(SettlementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get settlements for a specific user
     */
    @Transactional(readOnly = true)
    public List<SettlementResponse> getUserSettlements(String userId) {
        return settlementRepository.findByUserIdAsPayerOrPayee(userId).stream()
                .map(SettlementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create new settlement
     * Validates users via Feign client
     */
    @Transactional
    public SettlementResponse createSettlement(CreateSettlementRequest request) {
        // Validate payer and payee are different
        if (request.getPayerId().equals(request.getPayeeId())) {
            throw new BadRequestException("Payer and payee cannot be the same user");
        }

        // Validate payer exists via Feign client
        try {
            authServiceClient.getUser(request.getPayerId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", request.getPayerId());
        }

        // Validate payee exists via Feign client
        try {
            authServiceClient.getUser(request.getPayeeId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", request.getPayeeId());
        }

        // Create settlement
        Settlement settlement = new Settlement(
                "settlement_" + UUID.randomUUID().toString().substring(0, 8),
                request.getPayerId(),
                request.getPayeeId(),
                request.getAmount(),
                request.getNotes(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        settlement = settlementRepository.save(settlement);

        return SettlementResponse.fromEntity(settlement);
    }
}
