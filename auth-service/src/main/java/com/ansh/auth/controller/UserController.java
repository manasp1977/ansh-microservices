package com.ansh.auth.controller;

import com.ansh.auth.dto.request.ChangePasswordRequest;
import com.ansh.auth.dto.response.UserResponse;
import com.ansh.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for user management endpoints.
 * Note: API Gateway strips /api prefix, so routes here are /users/*
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /users - Get all users
     * Accessed via API Gateway as: GET /api/users
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("count", users.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /users/{id} - Get user by ID
     * Accessed via API Gateway as: GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /users/profile - Get current user profile
     * Uses X-User-Id header set by API Gateway
     * Accessed via API Gateway as: GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(@RequestHeader("X-User-Id") String userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /users/{id} - Update user
     * Accessed via API Gateway as: PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String id,
            @RequestBody UserResponse updateRequest) {
        UserResponse user = userService.updateUser(id, updateRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User updated successfully");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /users/{id} - Delete user
     * Accessed via API Gateway as: DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        response.put("user_id", id);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /users/change-password - Change current user's password
     * Uses X-User-Id header set by API Gateway
     * Accessed via API Gateway as: POST /api/users/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password changed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * GET /users/{id}/location - Get user location data
     * Accessed via listing-service (internal call, no JWT required from gateway)
     */
    @GetMapping("/{id}/location")
    public ResponseEntity<Map<String, Object>> getUserLocation(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("latitude", user.getLatitude());
        response.put("longitude", user.getLongitude());
        response.put("zipCode", user.getZipCode());

        return ResponseEntity.ok(response);
    }
}
