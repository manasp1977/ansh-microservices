package com.ansh.auth.controller;

import com.ansh.auth.dto.request.AdminLoginRequest;
import com.ansh.auth.dto.request.LoginRequest;
import com.ansh.auth.dto.request.SignupRequest;
import com.ansh.auth.dto.response.JwtResponse;
import com.ansh.auth.dto.response.UserResponse;
import com.ansh.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication endpoints.
 * Note: API Gateway strips /api prefix, so routes here are /auth/*
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /auth/login - User login
     * Accessed via API Gateway as: POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.authenticateUser(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", jwtResponse.getToken());
        response.put("user", jwtResponse.getUser());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/signup - User signup/registration
     * Accessed via API Gateway as: POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse user = authService.registerUser(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("user", user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/register - Alias for /auth/signup
     * For backward compatibility and convenience
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody SignupRequest request) {
        return signup(request);
    }

    /**
     * POST /auth/admin/login - Admin login
     * Accessed via API Gateway as: POST /api/auth/admin/login
     * Uses simple hardcoded credentials (in production, use proper admin user management)
     */
    @PostMapping("/admin/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        JwtResponse jwtResponse = authService.authenticateAdmin(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin login successful");
        response.put("token", jwtResponse.getToken());
        response.put("user", jwtResponse.getUser());
        response.put("isAdmin", true);

        return ResponseEntity.ok(response);
    }
}
