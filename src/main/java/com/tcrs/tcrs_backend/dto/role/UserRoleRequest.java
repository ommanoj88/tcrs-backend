package com.tcrs.tcrs_backend.dto.role;


import com.tcrs.tcrs_backend.entity.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRoleRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role is required")
    private Role role;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    // Constructors
    public UserRoleRequest() {}

    public UserRoleRequest(Long userId, Role role, String reason) {
        this.userId = userId;
        this.role = role;
        this.reason = reason;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
