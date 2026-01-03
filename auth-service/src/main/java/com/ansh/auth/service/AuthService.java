package com.ansh.auth.service;

import com.ansh.auth.dto.request.AdminLoginRequest;
import com.ansh.auth.dto.request.LoginRequest;
import com.ansh.auth.dto.request.SignupRequest;
import com.ansh.auth.dto.response.JwtResponse;
import com.ansh.auth.dto.response.UserResponse;
import com.ansh.auth.entity.User;
import com.ansh.auth.repository.UserRepository;
import com.ansh.common.exception.BadRequestException;
import com.ansh.common.exception.UnauthorizedException;
import com.ansh.common.security.JwtTokenProvider;
import com.ansh.common.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Service for authentication operations (login, signup).
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Authenticate user and generate JWT token
     */
    @Transactional(readOnly = true)
    public JwtResponse authenticateUser(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Generate JWT token
        UserPrincipal userPrincipal = createUserPrincipal(user);
        String token = tokenProvider.generateToken(userPrincipal);

        // Return response
        UserResponse userResponse = UserResponse.fromEntity(user);
        return new JwtResponse(token, userResponse);
    }

    /**
     * Register new user
     */
    @Transactional
    public UserResponse registerUser(SignupRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        User user = new User(
                "user_" + UUID.randomUUID().toString().substring(0, 8),
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getZipCode(),
                request.getLatitude(),
                request.getLongitude(),
                null
        );

        // Save user
        user = userRepository.save(user);

        // Return response
        return UserResponse.fromEntity(user);
    }

    /**
     * Authenticate admin and generate JWT token
     * Uses hardcoded credentials (in production, use proper admin user management)
     */
    public JwtResponse authenticateAdmin(AdminLoginRequest request) {
        // Simple admin credentials (in production, use proper authentication)
        final String ADMIN_USERNAME = "admin";
        final String ADMIN_PASSWORD = "admin123";

        if (!ADMIN_USERNAME.equals(request.getUsername()) ||
            !ADMIN_PASSWORD.equals(request.getPassword())) {
            throw new UnauthorizedException("Invalid admin credentials");
        }

        // Create admin user principal with ROLE_ADMIN
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        UserPrincipal adminPrincipal = new UserPrincipal(
                "admin",
                "Administrator",
                "admin@anshshare.com",
                "",
                authorities
        );

        // Generate JWT token
        String token = tokenProvider.generateToken(adminPrincipal);

        // Create admin user response
        UserResponse adminResponse = new UserResponse(
                "admin",
                "Administrator",
                "admin@anshshare.com",
                null,
                null,
                LocalDateTime.now()
        );

        return new JwtResponse(token, adminResponse);
    }

    /**
     * Create UserPrincipal from User entity
     */
    private UserPrincipal createUserPrincipal(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }
}
