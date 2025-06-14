package com.tcrs.tcrs_backend.service;


import com.tcrs.tcrs_backend.dto.auth.*;
import com.tcrs.tcrs_backend.entity.RefreshToken;
import com.tcrs.tcrs_backend.entity.Role;
import com.tcrs.tcrs_backend.entity.User;
import com.tcrs.tcrs_backend.exception.AuthException;
import com.tcrs.tcrs_backend.exception.BadRequestException;
import com.tcrs.tcrs_backend.repository.RefreshTokenRepository;
import com.tcrs.tcrs_backend.repository.UserRepository;
import com.tcrs.tcrs_backend.security.JwtUtils;
import com.tcrs.tcrs_backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse registerUser(RegisterRequest registerRequest) {
        logger.info("Attempting to register user with email: {}", registerRequest.getEmail());

        // Validate password confirmation
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already registered. Please use a different email.");
        }

        if (userRepository.existsByPhone(registerRequest.getPhone())) {
            throw new BadRequestException("Phone number is already registered. Please use a different phone number.");
        }

        // Create new user
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Set.of(Role.SME_USER)); // Default role
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate tokens
        String accessToken = jwtUtils.generateTokenFromPhone(savedUser.getPhone());        String refreshTokenValue = generateRefreshToken(savedUser);

        // Create response
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getRoles(),
                savedUser.getEmailVerified(),
                savedUser.getPhoneVerified()
        );

        return new AuthResponse(accessToken, refreshTokenValue, jwtUtils.getJwtExpirationMs(), userInfo);
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user: {}", loginRequest.getEmailOrPhone());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmailOrPhone(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Generate tokens
        String accessToken = jwtUtils.generateJwtToken(authentication);
        // Revoke existing refresh tokens for security
        // Revoke existing refresh tokens for security
        User user = userRepository.findActiveUserByPhone(userPrincipal.getPhone())
                .orElseThrow(() -> new AuthException("User not found"));

        refreshTokenRepository.revokeAllUserTokens(user);

        String refreshTokenValue = generateRefreshToken(user);

logger.info("User logged out from all devices: {}", user.getPhone());
        // Create response
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                userPrincipal.getId(),
                userPrincipal.getFirstName(),
                userPrincipal.getLastName(),
                userPrincipal.getEmail(),
                userPrincipal.getPhone(),
                user.getRoles(),
                user.getEmailVerified(),
                user.getPhoneVerified()
        );

        return new AuthResponse(accessToken, refreshTokenValue, jwtUtils.getJwtExpirationMs(), userInfo);
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findValidRefreshToken(requestRefreshToken, LocalDateTime.now())
                .orElseThrow(() -> new AuthException("Refresh token is not valid or has expired"));

        User user = refreshToken.getUser();

        // Generate new access token
        String newAccessToken = jwtUtils.generateTokenFromPhone(user.getPhone());

        // Optionally generate new refresh token (rotation)
        refreshTokenRepository.revokeAllUserTokens(user);
        String newRefreshToken = generateRefreshToken(user);

        logger.info("Token refreshed successfully for user: {}", user.getPhone());

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles(),
                user.getEmailVerified(),
                user.getPhoneVerified()
        );

        return new AuthResponse(newAccessToken, newRefreshToken, jwtUtils.getJwtExpirationMs(), userInfo);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElse(null);
            if (token != null) {
                token.setIsRevoked(true);
                refreshTokenRepository.save(token);
                logger.info("User logged out successfully");
            }
        }
    }

    public void logoutAllDevices(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        refreshTokenRepository.revokeAllUserTokens(user);
        logger.info("User logged out from all devices: {}", user.getEmail());
    }

    private String generateRefreshToken(User user) {
        // Generate unique refresh token
        String tokenValue = UUID.randomUUID().toString();

        // Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtUtils.getJwtRefreshExpirationMs() / 1000);

        RefreshToken refreshToken = new RefreshToken(tokenValue, expiresAt, user);
        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    public AuthResponse.UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new AuthException("Current user not found"));

            return new AuthResponse.UserInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRoles(),
                    user.getEmailVerified(),
                    user.getPhoneVerified()
            );
        }
        throw new AuthException("No authenticated user found");
    }

    // Cleanup method to remove expired tokens (can be scheduled)
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        logger.info("Cleaned up expired refresh tokens");
    }
}