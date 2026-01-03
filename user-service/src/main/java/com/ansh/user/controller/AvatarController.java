package com.ansh.user.controller;

import com.ansh.user.dto.response.UserProfileResponse;
import com.ansh.user.service.FileStorageService;
import com.ansh.user.service.UserProfileService;
import com.ansh.user.dto.request.UpdateProfileRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profiles")
public class AvatarController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserProfileService profileService;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @PostMapping("/me/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("error", "Please select a file to upload");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("error", "File size must be less than 2MB");
            return ResponseEntity.badRequest().body(response);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("error", "Only image files are allowed");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String filename = fileStorageService.storeFile(file, userId);
            String avatarUrl = "/profiles/avatars/" + filename;

            UpdateProfileRequest updateRequest = new UpdateProfileRequest();
            updateRequest.setAvatar(avatarUrl);
            UserProfileResponse profile = profileService.updateProfile(userId, updateRequest);

            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarUrl);
            response.put("profile", profile);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/avatars/{filename}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String filename) {
        try {
            byte[] imageData = fileStorageService.loadFile(filename);

            String contentType = "image/jpeg";
            if (filename.endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filename.endsWith(".webp")) {
                contentType = "image/webp";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageData.length);

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<Map<String, Object>> deleteAvatar(
            @RequestHeader("X-User-Id") String userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            UserProfileResponse currentProfile = profileService.getProfileByUserId(userId);
            String currentAvatar = currentProfile.getAvatar();

            if (currentAvatar != null && currentAvatar.startsWith("/profiles/avatars/")) {
                String filename = currentAvatar.substring("/profiles/avatars/".length());
                fileStorageService.deleteFile(filename);
            }

            UpdateProfileRequest updateRequest = new UpdateProfileRequest();
            updateRequest.setAvatar("");
            profileService.updateProfile(userId, updateRequest);

            response.put("message", "Avatar deleted successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Failed to delete avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
