package com.ansh.settlement.service;

import com.ansh.common.exception.ResourceNotFoundException;
import com.ansh.settlement.client.AuthServiceClient;
import com.ansh.settlement.client.ListingServiceClient;
import com.ansh.settlement.dto.ListingDTO;
import com.ansh.settlement.dto.UserDTO;
import com.ansh.settlement.dto.response.BalanceResponse;
import com.ansh.settlement.dto.response.UserTransactionResponse;
import com.ansh.settlement.entity.Settlement;
import com.ansh.settlement.repository.SettlementRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating user balances and transaction details.
 * In microservices: Uses Feign client to fetch sold listings from Listing Service.
 */
@Service
public class BalanceCalculationService {

    @Autowired
    private ListingServiceClient listingServiceClient;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    /**
     * Calculate balances for all users
     * Fetches sold listings via Feign client
     */
    @Transactional(readOnly = true)
    public BalanceResponse calculateUserBalances() {
        Map<String, BalanceResponse.UserBalance> balances = new HashMap<>();

        // Get all sold listings via Feign client
        List<ListingDTO> soldListings = listingServiceClient.getAllSoldListings().getListings();

        // Calculate transaction balances
        for (ListingDTO listing : soldListings) {
            String sellerId = listing.getSellerId();
            String buyerId = listing.getBuyerId();
            BigDecimal amount = listing.getTotalPrice();

            // Initialize balances if not exist (fetch user details via Feign)
            if (!balances.containsKey(sellerId)) {
                balances.put(sellerId, createUserBalance(sellerId));
            }
            if (!balances.containsKey(buyerId)) {
                balances.put(buyerId, createUserBalance(buyerId));
            }

            // Buyer owes seller
            BalanceResponse.UserBalance buyerBalance = balances.get(buyerId);
            buyerBalance.setOwes(buyerBalance.getOwes().add(amount));
            buyerBalance.setBought(buyerBalance.getBought().add(amount));
            buyerBalance.setNet(buyerBalance.getNet().subtract(amount));

            // Seller is owed by buyer
            BalanceResponse.UserBalance sellerBalance = balances.get(sellerId);
            sellerBalance.setOwed(sellerBalance.getOwed().add(amount));
            sellerBalance.setSold(sellerBalance.getSold().add(amount));
            sellerBalance.setNet(sellerBalance.getNet().add(amount));
        }

        // Process settlements
        List<Settlement> settlements = settlementRepository.findAll();
        for (Settlement settlement : settlements) {
            String payerId = settlement.getPayerId();
            String payeeId = settlement.getPayeeId();
            BigDecimal amount = settlement.getAmount();

            // Initialize balances if not exist
            if (!balances.containsKey(payerId)) {
                balances.put(payerId, createUserBalance(payerId));
            }
            if (!balances.containsKey(payeeId)) {
                balances.put(payeeId, createUserBalance(payeeId));
            }

            // Payer made payment
            BalanceResponse.UserBalance payerBalance = balances.get(payerId);
            payerBalance.setPaid(payerBalance.getPaid().add(amount));
            payerBalance.setNet(payerBalance.getNet().add(amount));

            // Payee received payment
            BalanceResponse.UserBalance payeeBalance = balances.get(payeeId);
            payeeBalance.setReceived(payeeBalance.getReceived().add(amount));
            payeeBalance.setNet(payeeBalance.getNet().subtract(amount));
        }

        return new BalanceResponse(balances);
    }

    /**
     * Get user's transactions with other users
     * Fetches sold listings via Feign client
     */
    @Transactional(readOnly = true)
    public UserTransactionResponse getUserTransactionsWithOthers(String userId) {
        // Get user details via Feign client
        UserDTO user;
        try {
            user = authServiceClient.getUser(userId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Map<String, UserTransactionResponse.OtherUserTransaction> transactionsMap = new HashMap<>();

        // Get sold listings via Feign client
        List<ListingDTO> soldListings = listingServiceClient.getSoldListingsByUser(userId).getListings();

        for (ListingDTO listing : soldListings) {
            String sellerId = listing.getSellerId();
            String buyerId = listing.getBuyerId();

            if (!userId.equals(sellerId) && !userId.equals(buyerId)) {
                continue;
            }

            String otherUserId = userId.equals(sellerId) ? buyerId : sellerId;

            // Get other user details via Feign client
            UserDTO otherUser;
            try {
                otherUser = authServiceClient.getUser(otherUserId);
            } catch (FeignException.NotFound e) {
                continue; // Skip if user not found
            }

            UserTransactionResponse.OtherUserTransaction otherUserTxn =
                    transactionsMap.computeIfAbsent(otherUserId, k ->
                            new UserTransactionResponse.OtherUserTransaction(
                                    otherUser.getName(),
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    new ArrayList<>(),
                                    new ArrayList<>()
                            )
                    );

            // Add transaction
            UserTransactionResponse.TransactionInfo txn =
                    new UserTransactionResponse.TransactionInfo(
                            listing.getItemName(),
                            listing.getTotalPrice(),
                            listing.getPurchasedDate(),
                            userId.equals(sellerId) ? "sold" : "bought"
                    );
            otherUserTxn.getTransactions().add(txn);

            // Update balances
            if (userId.equals(sellerId)) {
                otherUserTxn.setOwed(otherUserTxn.getOwed().add(listing.getTotalPrice()));
                otherUserTxn.setNet(otherUserTxn.getNet().add(listing.getTotalPrice()));
            } else {
                otherUserTxn.setOwes(otherUserTxn.getOwes().add(listing.getTotalPrice()));
                otherUserTxn.setNet(otherUserTxn.getNet().subtract(listing.getTotalPrice()));
            }
        }

        // Process settlements
        List<Settlement> settlements = settlementRepository.findByUserIdAsPayerOrPayee(userId);

        for (Settlement settlement : settlements) {
            String payerId = settlement.getPayerId();
            String payeeId = settlement.getPayeeId();

            String otherUserId = userId.equals(payerId) ? payeeId : payerId;

            // Get other user details via Feign client
            UserDTO otherUser;
            try {
                otherUser = authServiceClient.getUser(otherUserId);
            } catch (FeignException.NotFound e) {
                continue; // Skip if user not found
            }

            UserTransactionResponse.OtherUserTransaction otherUserTxn =
                    transactionsMap.computeIfAbsent(otherUserId, k ->
                            new UserTransactionResponse.OtherUserTransaction(
                                    otherUser.getName(),
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    new ArrayList<>(),
                                    new ArrayList<>()
                            )
                    );

            // Add settlement
            UserTransactionResponse.SettlementInfo settlementInfo =
                    new UserTransactionResponse.SettlementInfo(
                            settlement.getAmount(),
                            settlement.getSettledDate(),
                            settlement.getNotes(),
                            userId.equals(payerId) ? "paid" : "received"
                    );
            otherUserTxn.getSettlements().add(settlementInfo);

            // Update balances
            if (userId.equals(payerId)) {
                otherUserTxn.setNet(otherUserTxn.getNet().add(settlement.getAmount()));
            } else {
                otherUserTxn.setNet(otherUserTxn.getNet().subtract(settlement.getAmount()));
            }
        }

        return new UserTransactionResponse(
                user.getId(),
                user.getName(),
                transactionsMap
        );
    }

    /**
     * Helper method to create user balance
     * Fetches user details via Feign client
     */
    private BalanceResponse.UserBalance createUserBalance(String userId) {
        UserDTO user;
        try {
            user = authServiceClient.getUser(userId);
        } catch (FeignException.NotFound e) {
            // Return balance with unknown user
            return new BalanceResponse.UserBalance(
                    userId,
                    "Unknown User",
                    "",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        return new BalanceResponse.UserBalance(
                user.getId(),
                user.getName(),
                user.getEmail(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
