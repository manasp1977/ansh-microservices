package com.ansh.settlement.controller;

import com.ansh.settlement.dto.request.CreateSettlementRequest;
import com.ansh.settlement.dto.response.BalanceResponse;
import com.ansh.settlement.dto.response.SettlementResponse;
import com.ansh.settlement.dto.response.UserTransactionResponse;
import com.ansh.settlement.service.BalanceCalculationService;
import com.ansh.settlement.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for settlement and balance endpoints.
 * Note: API Gateway strips /api prefix, so routes here don't have /api
 */
@RestController
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private BalanceCalculationService balanceCalculationService;

    /**
     * GET /settlements - Get all settlements
     * Accessed via API Gateway as: GET /api/settlements
     */
    @GetMapping("/settlements")
    public ResponseEntity<Map<String, Object>> getAllSettlements() {
        List<SettlementResponse> settlements = settlementService.getAllSettlements();

        Map<String, Object> response = new HashMap<>();
        response.put("settlements", settlements);
        response.put("count", settlements.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /settlements/user/{userId} - Get user's settlements
     * Accessed via API Gateway as: GET /api/settlements/user/{userId}
     */
    @GetMapping("/settlements/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserSettlements(@PathVariable String userId) {
        List<SettlementResponse> settlements = settlementService.getUserSettlements(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("settlements", settlements);
        response.put("count", settlements.size());
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /settlements - Create new settlement
     * Accessed via API Gateway as: POST /api/settlements
     */
    @PostMapping("/settlements")
    public ResponseEntity<Map<String, Object>> createSettlement(
            @Valid @RequestBody CreateSettlementRequest request) {

        SettlementResponse settlement = settlementService.createSettlement(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Settlement recorded successfully");
        response.put("settlement", Map.of(
                "id", settlement.getId(),
                "payer_id", settlement.getPayerId(),
                "payee_id", settlement.getPayeeId(),
                "amount", settlement.getAmount(),
                "settled_date", settlement.getSettledDate()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /balances - Calculate balances for all users
     * Accessed via API Gateway as: GET /api/balances
     * Fetches sold listings from Listing Service via Feign
     */
    @GetMapping("/balances")
    public ResponseEntity<BalanceResponse> calculateBalances() {
        BalanceResponse balances = balanceCalculationService.calculateUserBalances();
        return ResponseEntity.ok(balances);
    }

    /**
     * GET /balances/user/{userId} - Get user's transaction details with others
     * Accessed via API Gateway as: GET /api/balances/user/{userId}
     * Fetches sold listings from Listing Service via Feign
     */
    @GetMapping("/balances/user/{userId}")
    public ResponseEntity<UserTransactionResponse> getUserTransactions(@PathVariable String userId) {
        UserTransactionResponse transactions = balanceCalculationService.getUserTransactionsWithOthers(userId);
        return ResponseEntity.ok(transactions);
    }
}
