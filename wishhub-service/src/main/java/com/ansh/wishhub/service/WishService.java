package com.ansh.wishhub.service;

import com.ansh.wishhub.client.AuthServiceClient;
import com.ansh.wishhub.client.ReceiptServiceClient;
import com.ansh.wishhub.dto.CreateWishRequest;
import com.ansh.wishhub.dto.UserDTO;
import com.ansh.wishhub.dto.WishListResponse;
import com.ansh.wishhub.dto.WishResponse;
import com.ansh.wishhub.entity.Wish;
import com.ansh.wishhub.enums.WishStatus;
import com.ansh.wishhub.repository.WishRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WishService {

    private static final Logger log = LoggerFactory.getLogger(WishService.class);

    private final WishRepository wishRepository;
    private final AuthServiceClient authServiceClient;
    private final ReceiptServiceClient receiptServiceClient;
    private final Map<String, String> userNameCache = new ConcurrentHashMap<>();

    public WishService(WishRepository wishRepository, AuthServiceClient authServiceClient, ReceiptServiceClient receiptServiceClient) {
        this.wishRepository = wishRepository;
        this.authServiceClient = authServiceClient;
        this.receiptServiceClient = receiptServiceClient;
    }

    public WishListResponse getAllWishes() {
        List<Wish> wishes = wishRepository.findAllByOrderByCreatedAtDesc();
        List<WishResponse> responses = wishes.stream()
                .map(this::enrichWithUserNames)
                .collect(Collectors.toList());
        return new WishListResponse(responses);
    }

    public WishListResponse getOpenWishes() {
        List<Wish> wishes = wishRepository.findByStatusOrderByCreatedAtDesc(WishStatus.OPEN);
        List<WishResponse> responses = wishes.stream()
                .map(this::enrichWithUserNames)
                .collect(Collectors.toList());
        return new WishListResponse(responses);
    }

    public WishListResponse getWishesByUser(String userId) {
        List<Wish> wishes = wishRepository.findByUserId(userId);
        List<WishResponse> responses = wishes.stream()
                .map(this::enrichWithUserNames)
                .collect(Collectors.toList());
        return new WishListResponse(responses);
    }

    public WishListResponse getClaimedWishesByUser(String claimerId) {
        List<Wish> wishes = wishRepository.findByClaimerId(claimerId);
        List<WishResponse> responses = wishes.stream()
                .map(this::enrichWithUserNames)
                .collect(Collectors.toList());
        return new WishListResponse(responses);
    }

    public WishResponse getWishById(String id) {
        Wish wish = wishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wish not found: " + id));
        return enrichWithUserNames(wish);
    }

    @Transactional
    public WishResponse createWish(String userId, CreateWishRequest request) {
        Wish wish = new Wish();
        wish.setId("wish_" + UUID.randomUUID().toString().substring(0, 8));
        wish.setUserId(userId);
        wish.setItemName(request.getItemName());
        wish.setDescription(request.getDescription());
        wish.setQuantity(request.getQuantity());
        wish.setImageUrl(request.getImageUrl());
        wish.setStatus(WishStatus.OPEN);
        wish.setCreatedAt(LocalDateTime.now());
        wish.setUpdatedAt(LocalDateTime.now());

        Wish saved = wishRepository.save(wish);
        log.info("Created wish: {} for user: {}", saved.getId(), userId);
        return enrichWithUserNames(saved);
    }

    @Transactional
    public WishResponse claimWish(String wishId, String claimerId) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new RuntimeException("Wish not found: " + wishId));

        if (wish.getStatus() != WishStatus.OPEN) {
            throw new RuntimeException("Wish is not open for claiming");
        }

        if (wish.getUserId().equals(claimerId)) {
            throw new RuntimeException("Cannot claim your own wish");
        }

        wish.claim(claimerId);
        wish.setUpdatedAt(LocalDateTime.now());
        Wish saved = wishRepository.save(wish);
        log.info("Wish {} claimed by user {}", wishId, claimerId);
        return enrichWithUserNames(saved);
    }

    @Transactional
    public WishResponse unclaimWish(String wishId, String userId) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new RuntimeException("Wish not found: " + wishId));

        if (wish.getStatus() != WishStatus.CLAIMED) {
            throw new RuntimeException("Wish is not claimed");
        }

        if (!wish.getClaimerId().equals(userId)) {
            throw new RuntimeException("You can only unclaim wishes you have claimed");
        }

        // Delete any receipts created from this wish
        try {
            Map<String, Object> result = receiptServiceClient.deleteReceiptsByWishId(wishId);
            log.info("Deleted receipts for wish {}: {}", wishId, result);
        } catch (Exception e) {
            log.warn("Failed to delete receipts for wish {}: {}", wishId, e.getMessage());
            // Continue with unclaim even if receipt deletion fails
        }

        wish.setStatus(WishStatus.OPEN);
        wish.setClaimerId(null);
        wish.setClaimedDate(null);
        wish.setUpdatedAt(LocalDateTime.now());
        Wish saved = wishRepository.save(wish);
        log.info("Wish {} unclaimed by user {}", wishId, userId);
        return enrichWithUserNames(saved);
    }

    @Transactional
    public void deleteWish(String wishId, String userId) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new RuntimeException("Wish not found: " + wishId));

        if (!wish.getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own wishes");
        }

        wishRepository.delete(wish);
        log.info("Deleted wish: {} by user: {}", wishId, userId);
    }

    @Transactional
    public WishResponse updateWishImage(String wishId, String userId, String imageUrl) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new RuntimeException("Wish not found: " + wishId));

        if (!wish.getUserId().equals(userId)) {
            throw new RuntimeException("You can only update your own wishes");
        }

        wish.setImageUrl(imageUrl);
        wish.setUpdatedAt(LocalDateTime.now());
        Wish saved = wishRepository.save(wish);
        return enrichWithUserNames(saved);
    }

    private WishResponse enrichWithUserNames(Wish wish) {
        WishResponse response = WishResponse.fromEntity(wish);
        response.setUserName(getUserName(wish.getUserId()));
        if (wish.getClaimerId() != null) {
            response.setClaimerName(getUserName(wish.getClaimerId()));
        }
        return response;
    }

    private String getUserName(String userId) {
        if (userId == null) return null;

        return userNameCache.computeIfAbsent(userId, id -> {
            try {
                UserDTO user = authServiceClient.getUserById(id);
                return user != null ? user.getName() : id;
            } catch (Exception e) {
                log.warn("Failed to fetch user name for {}: {}", id, e.getMessage());
                return id;
            }
        });
    }
}
