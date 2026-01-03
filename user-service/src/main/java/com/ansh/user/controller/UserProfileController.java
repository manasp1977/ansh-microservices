package com.ansh.user.controller;

import com.ansh.user.dto.request.UpdateProfileRequest;
import com.ansh.user.dto.response.UserProfileResponse;
import com.ansh.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profiles")
public class UserProfileController {

    @Autowired
    private UserProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {
        UserProfileResponse profile = profileService.getProfileByUserId(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("profile", profile);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProfileById(@PathVariable String id) {
        UserProfileResponse profile = profileService.getProfileById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("profile", profile);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getProfileByUserId(@PathVariable String userId) {
        UserProfileResponse profile = profileService.getProfileByUserId(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("profile", profile);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProfiles() {
        List<UserProfileResponse> profiles = profileService.getAllProfiles();
        Map<String, Object> response = new HashMap<>();
        response.put("profiles", profiles);
        response.put("count", profiles.size());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = profileService.updateProfile(userId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("profile", profile);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, Object>> deleteMyProfile(
            @RequestHeader("X-User-Id") String userId) {
        profileService.deleteProfile(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile deleted successfully");
        return ResponseEntity.ok(response);
    }
}
