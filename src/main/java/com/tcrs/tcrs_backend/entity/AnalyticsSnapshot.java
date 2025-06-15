package com.tcrs.tcrs_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.tcrs.tcrs_backend.entity.SnapShotType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDateTime snapshotDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_type", nullable = false)
    private SnapShotType snapshotType; // DAILY, WEEKLY, MONTHLY

    // Business Metrics
    @Column(name = "total_businesses")
    private Long totalBusinesses;

    @Column(name = "new_businesses_count")
    private Long newBusinessesCount;

    @Column(name = "verified_businesses_count")
    private Long verifiedBusinessesCount;

    // Credit Metrics
    @Column(name = "total_credit_reports")
    private Long totalCreditReports;

    @Column(name = "average_credit_score", precision = 5, scale = 2)
    private BigDecimal averageCreditScore;

    @Column(name = "high_risk_businesses")
    private Long highRiskBusinesses;

    @Column(name = "low_risk_businesses")
    private Long lowRiskBusinesses;

    // Payment Metrics
    @Column(name = "total_payment_records")
    private Long totalPaymentRecords;

    @Column(name = "on_time_payments_percentage", precision = 5, scale = 2)
    private BigDecimal onTimePaymentsPercentage;

    @Column(name = "overdue_amount", precision = 15, scale = 2)
    private BigDecimal overdueAmount;

    // Trade Reference Metrics
    @Column(name = "total_trade_references")
    private Long totalTradeReferences;

    @Column(name = "verified_trade_references")
    private Long verifiedTradeReferences;

    @Column(name = "positive_recommendations_percentage", precision = 5, scale = 2)
    private BigDecimal positiveRecommendationsPercentage;

    // Alert Metrics
    @Column(name = "total_alerts_generated")
    private Long totalAlertsGenerated;

    @Column(name = "critical_alerts_count")
    private Long criticalAlertsCount;

    @Column(name = "alerts_acknowledged_percentage", precision = 5, scale = 2)
    private BigDecimal alertsAcknowledgedPercentage;

    // User Activity Metrics
    @Column(name = "active_users_count")
    private Long activeUsersCount;

    @Column(name = "total_logins")
    private Long totalLogins;

    @Column(name = "total_searches")
    private Long totalSearches;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
