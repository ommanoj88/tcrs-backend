package com.tcrs.tcrs_backend.dto.role;

import com.tcrs.tcrs_backend.entity.Role;

import java.time.LocalDateTime;

public class RoleHistoryResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Role oldRole;
    private Role newRole;
    private String changedByName;
    private String reason;
    private LocalDateTime changedAt;

    // Constructors
    public RoleHistoryResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Role getOldRole() { return oldRole; }
    public void setOldRole(Role oldRole) { this.oldRole = oldRole; }

    public Role getNewRole() { return newRole; }
    public void setNewRole(Role newRole) { this.newRole = newRole; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}