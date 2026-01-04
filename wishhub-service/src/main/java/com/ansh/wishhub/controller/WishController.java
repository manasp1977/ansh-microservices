package com.ansh.wishhub.controller;

import com.ansh.wishhub.dto.CreateWishRequest;
import com.ansh.wishhub.dto.WishListResponse;
import com.ansh.wishhub.dto.WishResponse;
import com.ansh.wishhub.service.FileStorageService;
import com.ansh.wishhub.service.WishService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/wishes")
public class WishController {

    private static final Logger log = LoggerFactory.getLogger(WishController.class);

    private final WishService wishService;
    private final FileStorageService fileStorageService;

    public WishController(WishService wishService, FileStorageService fileStorageService) {
        this.wishService = wishService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<WishListResponse> getAllWishes() {
        log.debug("GET /wishes - fetching all wishes");
        return ResponseEntity.ok(wishService.getOpenWishes());
    }

    @GetMapping("/all")
    public ResponseEntity<WishListResponse> getAllWishesIncludingClaimed() {
        log.debug("GET /wishes/all - fetching all wishes including claimed");
        return ResponseEntity.ok(wishService.getAllWishes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WishResponse> getWishById(@PathVariable String id) {
        log.debug("GET /wishes/{}", id);
        return ResponseEntity.ok(wishService.getWishById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WishListResponse> getWishesByUser(@PathVariable String userId) {
        log.debug("GET /wishes/user/{}", userId);
        return ResponseEntity.ok(wishService.getWishesByUser(userId));
    }

    @GetMapping("/my")
    public ResponseEntity<WishListResponse> getMyWishes(@RequestHeader("X-User-Id") String userId) {
        log.debug("GET /wishes/my for user {}", userId);
        return ResponseEntity.ok(wishService.getWishesByUser(userId));
    }

    @GetMapping("/claimed")
    public ResponseEntity<WishListResponse> getMyClaims(@RequestHeader("X-User-Id") String userId) {
        log.debug("GET /wishes/claimed for user {}", userId);
        return ResponseEntity.ok(wishService.getClaimedWishesByUser(userId));
    }

    @PostMapping
    public ResponseEntity<WishResponse> createWish(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateWishRequest request) {
        log.debug("POST /wishes - creating wish for user {}", userId);
        WishResponse response = wishService.createWish(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<WishResponse> claimWish(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.debug("POST /wishes/{}/claim by user {}", id, userId);
        WishResponse response = wishService.claimWish(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unclaim")
    public ResponseEntity<WishResponse> unclaimWish(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.debug("POST /wishes/{}/unclaim by user {}", id, userId);
        WishResponse response = wishService.unclaimWish(id, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteWish(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.debug("DELETE /wishes/{} by user {}", id, userId);
        wishService.deleteWish(id, userId);
        return ResponseEntity.ok(Map.of("message", "Wish deleted successfully"));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) {
        log.debug("POST /wishes/{}/image - uploading image", id);

        try {
            String imageUrl = fileStorageService.storeFile(file);
            wishService.updateWishImage(id, userId, imageUrl);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            log.error("Failed to upload image for wish {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<WishResponse> updateImage(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> body) {
        log.debug("PUT /wishes/{}/image", id);
        String imageUrl = body.get("imageUrl");
        WishResponse response = wishService.updateWishImage(id, userId, imageUrl);
        return ResponseEntity.ok(response);
    }
}
