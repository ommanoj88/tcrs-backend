package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.role.RoleHistoryResponse;
import com.tcrs.tcrs_backend.dto.role.UserResponse;
import com.tcrs.tcrs_backend.dto.role.UserRoleRequest;
import com.tcrs.tcrs_backend.entity.Role;
import com.tcrs.tcrs_backend.entity.User;
import com.tcrs.tcrs_backend.entity.UserRoleHistory;
import com.tcrs.tcrs_backend.exception.BadRequestException;
import com.tcrs.tcrs_backend.exception.ResourceNotFoundException;
import com.tcrs.tcrs_backend.repository.UserRepository;
import com.tcrs.tcrs_backend.repository.UserRoleHistoryRepository;
import com.tcrs.tcrs_backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleHistoryRepository roleHistoryRepository;

    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users for role management");

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public Page<UserResponse> getAllUsers(int page, int size) {
        logger.info("Fetching users with pagination - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> usersPage = userRepository.findAll(pageable);

        return usersPage.map(this::convertToUserResponse);
    }

    public UserResponse assignRole(UserRoleRequest request) {
        logger.info("Assigning role {} to user {}", request.getRole(), request.getUserId());

        // Get current admin user
        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User currentUser = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Get target user
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Validate role assignment
        validateRoleAssignment(currentUser, targetUser, request.getRole());

        // Get current roles for history
        Set<Role> currentRoles = targetUser.getRoles();
        Role oldRole = currentRoles.isEmpty() ? null : currentRoles.iterator().next();

        // Update user role
        targetUser.getRoles().clear();
        targetUser.getRoles().add(request.getRole());
        User updatedUser = userRepository.save(targetUser);

        // Record role change history
        UserRoleHistory roleHistory = new UserRoleHistory(
                targetUser,
                oldRole,
                request.getRole(),
                currentUser,
                request.getReason()
        );
        roleHistoryRepository.save(roleHistory);

        logger.info("Role {} assigned to user {} successfully", request.getRole(), targetUser.getEmail());

        return convertToUserResponse(updatedUser);
    }

    public UserResponse toggleUserStatus(Long userId, String reason) {
        logger.info("Toggling user status for user ID: {}", userId);

        UserPrincipal currentUserPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User currentUser = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Prevent self-deactivation
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new BadRequestException("You cannot deactivate your own account");
        }

        // Toggle user status
        targetUser.setIsActive(!targetUser.getIsActive());
        User updatedUser = userRepository.save(targetUser);

        logger.info("User status toggled for user: {} to {}", targetUser.getEmail(), updatedUser.getIsActive());

        return convertToUserResponse(updatedUser);
    }

    public List<RoleHistoryResponse> getRoleHistory() {
        logger.info("Fetching role change history");

        List<UserRoleHistory> history = roleHistoryRepository.findAllOrderByChangedAtDesc();

        return history.stream()
                .map(this::convertToRoleHistoryResponse)
                .collect(Collectors.toList());
    }

    public List<RoleHistoryResponse> getUserRoleHistory(Long userId) {
        logger.info("Fetching role history for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<UserRoleHistory> history = roleHistoryRepository.findByUserOrderByChangedAtDesc(user);

        return history.stream()
                .map(this::convertToRoleHistoryResponse)
                .collect(Collectors.toList());
    }

    public List<Role> getAvailableRoles() {
        return List.of(Role.values());
    }

    private void validateRoleAssignment(User currentUser, User targetUser, Role newRole) {
        // Check if current user has admin role
        if (!currentUser.getRoles().contains(Role.ADMIN)) {
            throw new BadRequestException("Only admins can assign roles");
        }

        // Prevent self-role modification for certain scenarios
        if (currentUser.getId().equals(targetUser.getId()) && newRole != Role.ADMIN) {
            throw new BadRequestException("Admin cannot remove their own admin role");
        }

        // Additional validation can be added here
        logger.debug("Role assignment validation passed for user: {}", targetUser.getEmail());
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRoles(user.getRoles());
        response.setEmailVerified(user.getEmailVerified());
        response.setPhoneVerified(user.getPhoneVerified());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    private RoleHistoryResponse convertToRoleHistoryResponse(UserRoleHistory history) {
        RoleHistoryResponse response = new RoleHistoryResponse();
        response.setId(history.getId());
        response.setUserId(history.getUser().getId());
        response.setUserName(history.getUser().getFullName());
        response.setOldRole(history.getOldRole());
        response.setNewRole(history.getNewRole());
        response.setChangedByName(history.getChangedBy().getFullName());
        response.setReason(history.getReason());
        response.setChangedAt(history.getChangedAt());

        return response;
    }
}
