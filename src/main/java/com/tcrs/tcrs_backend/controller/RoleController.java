package com.tcrs.tcrs_backend.controller;

import com.tcrs.tcrs_backend.dto.auth.ApiResponse;
import com.tcrs.tcrs_backend.dto.role.RoleHistoryResponse;
import com.tcrs.tcrs_backend.dto.role.UserResponse;
import com.tcrs.tcrs_backend.dto.role.UserRoleRequest;
import com.tcrs.tcrs_backend.entity.Role;
import com.tcrs.tcrs_backend.service.RoleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        logger.info("Get all users request received");

        List<UserResponse> users = roleService.getAllUsers();

        ApiResponse<List<UserResponse>> response = new ApiResponse<>(
                true,
                "Users retrieved successfully",
                users
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get paginated users request received - page: {}, size: {}", page, size);

        Page<UserResponse> users = roleService.getAllUsers(page, size);

        ApiResponse<Page<UserResponse>> response = new ApiResponse<>(
                true,
                "Users retrieved successfully",
                users
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(@Valid @RequestBody UserRoleRequest request) {
        logger.info("Role assignment request received for user: {}", request.getUserId());

        UserResponse userResponse = roleService.assignRole(request);

        ApiResponse<UserResponse> response = new ApiResponse<>(
                true,
                "Role assigned successfully",
                userResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/toggle-status/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> request) {
        logger.info("Toggle user status request received for user: {}", userId);

        String reason = null;
        if (request != null && request.containsKey("reason")) {
            reason = request.get("reason");
        }

        UserResponse userResponse = roleService.toggleUserStatus(userId, reason);

        ApiResponse<UserResponse> response = new ApiResponse<>(
                true,
                "User status updated successfully",
                userResponse
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleHistoryResponse>>> getRoleHistory() {
        logger.info("Get role history request received");

        List<RoleHistoryResponse> history = roleService.getRoleHistory();

        ApiResponse<List<RoleHistoryResponse>> response = new ApiResponse<>(
                true,
                "Role history retrieved successfully",
                history
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleHistoryResponse>>> getUserRoleHistory(@PathVariable Long userId) {
        logger.info("Get user role history request received for user: {}", userId);

        List<RoleHistoryResponse> history = roleService.getUserRoleHistory(userId);

        ApiResponse<List<RoleHistoryResponse>> response = new ApiResponse<>(
                true,
                "User role history retrieved successfully",
                history
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Role>>> getAvailableRoles() {
        logger.info("Get available roles request received");

        List<Role> roles = roleService.getAvailableRoles();

        ApiResponse<List<Role>> response = new ApiResponse<>(
                true,
                "Available roles retrieved successfully",
                roles
        );

        return ResponseEntity.ok(response);
    }
}