package com.tcrs.tcrs_backend.controller;


import com.tcrs.tcrs_backend.dto.auth.*;
import com.tcrs.tcrs_backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Registration request received for email: {}", registerRequest.getEmail());

        AuthResponse authResponse = authService.registerUser(registerRequest);

        ApiResponse<AuthResponse> response = new ApiResponse<>(
                true,
                "User registered successfully",
                authResponse
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login request received for: {}", loginRequest.getEmailOrPhone());

        AuthResponse authResponse = authService.authenticateUser(loginRequest);

        ApiResponse<AuthResponse> response = new ApiResponse<>(
                true,
                "User authenticated successfully",
                authResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        logger.info("Token refresh request received");

        AuthResponse authResponse = authService.refreshToken(refreshTokenRequest);

        ApiResponse<AuthResponse> response = new ApiResponse<>(
                true,
                "Token refreshed successfully",
                authResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SME_USER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Void>> logoutUser(@RequestBody(required = false) Map<String, String> request) {
        logger.info("Logout request received");

        String refreshToken = null;
        if (request != null && request.containsKey("refreshToken")) {
            refreshToken = request.get("refreshToken");
        }

        authService.logout(refreshToken);

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "User logged out successfully",
                null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout-all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SME_USER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices() {
        logger.info("Logout all devices request received");

        AuthResponse.UserInfo currentUser = authService.getCurrentUser();
        authService.logoutAllDevices(currentUser.getId());

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "User logged out from all devices successfully",
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SME_USER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser() {
        logger.info("Get current user request received");

        AuthResponse.UserInfo userInfo = authService.getCurrentUser();

        ApiResponse<AuthResponse.UserInfo> response = new ApiResponse<>(
                true,
                "Current user retrieved successfully",
                userInfo
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SME_USER') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateToken(HttpServletRequest request) {
        logger.info("Token validation request received");

        // If we reach here, token is valid (due to @PreAuthorize)
        ApiResponse<Map<String, Boolean>> response = new ApiResponse<>(
                true,
                "Token is valid",
                Map.of("valid", true)
        );

        return ResponseEntity.ok(response);
    }
}