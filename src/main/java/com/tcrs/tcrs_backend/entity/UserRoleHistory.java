package com.tcrs.tcrs_backend.entity;


import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_role_history")
@EntityListeners(AuditingEntityListener.class)
public class UserRoleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_role")
    private Role oldRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_role", nullable = false)
    private Role newRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "reason")
    private String reason;

    @CreatedDate
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    // Constructors
    public UserRoleHistory() {}

    public UserRoleHistory(User user, Role oldRole, Role newRole, User changedBy, String reason) {
        this.user = user;
        this.oldRole = oldRole;
        this.newRole = newRole;
        this.changedBy = changedBy;
        this.reason = reason;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Role getOldRole() { return oldRole; }
    public void setOldRole(Role oldRole) { this.oldRole = oldRole; }

    public Role getNewRole() { return newRole; }
    public void setNewRole(Role newRole) { this.newRole = newRole; }

    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}