package com.tcrs.tcrs_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kyc_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false, unique = true)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private KycStatus kycStatus = KycStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_level", nullable = false)
    private KycLevel kycLevel = KycLevel.BASIC;

    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @Column(name = "risk_score")
    private Double riskScore; // Risk assessment score (0-100)

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category")
    private RiskCategory riskCategory = RiskCategory.LOW;

    // FIXED: Remove the @OneToMany mapping here since KycDocument has business, not kycProfile
    // We'll load documents via the service layer using businessId
    @Transient
    private List<KycDocument> documents = new ArrayList<>();

    @Column(name = "business_verification_status")
    private Boolean businessVerificationStatus = false;

    @Column(name = "director_verification_status")
    private Boolean directorVerificationStatus = false;

    @Column(name = "financial_verification_status")
    private Boolean financialVerificationStatus = false;

    @Column(name = "address_verification_status")
    private Boolean addressVerificationStatus = false;

    @Column(name = "banking_verification_status")
    private Boolean bankingVerificationStatus = false;

    @Column(name = "last_verification_date")
    private LocalDateTime lastVerificationDate;

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    @Column(name = "assigned_officer")
    private String assignedOfficer;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "compliance_flags", columnDefinition = "TEXT")
    private String complianceFlags; // JSON array of compliance issues

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

    // Helper methods
    public boolean isCompleted() {
        return kycStatus == KycStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return kycStatus == KycStatus.IN_PROGRESS;
    }

    public boolean isPending() {
        return kycStatus == KycStatus.PENDING_REVIEW;
    }

    public boolean isRejected() {
        return kycStatus == KycStatus.REJECTED;
    }

    public void updateCompletionPercentage() {
        int totalChecks = 5; // business, director, financial, address, banking
        int completedChecks = 0;

        if (Boolean.TRUE.equals(businessVerificationStatus)) completedChecks++;
        if (Boolean.TRUE.equals(directorVerificationStatus)) completedChecks++;
        if (Boolean.TRUE.equals(financialVerificationStatus)) completedChecks++;
        if (Boolean.TRUE.equals(addressVerificationStatus)) completedChecks++;
        if (Boolean.TRUE.equals(bankingVerificationStatus)) completedChecks++;

        this.completionPercentage = (completedChecks * 100) / totalChecks;

        // Update KYC status based on completion
        if (completionPercentage == 100) {
            this.kycStatus = KycStatus.COMPLETED;
        } else if (completionPercentage > 0) {
            this.kycStatus = KycStatus.IN_PROGRESS;
        } else {
            this.kycStatus = KycStatus.NOT_STARTED;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void calculateRiskScore() {
        double score = 0.0;

        // Business verification (20%)
        if (Boolean.TRUE.equals(businessVerificationStatus)) score += 20;

        // Director verification (20%)
        if (Boolean.TRUE.equals(directorVerificationStatus)) score += 20;

        // Financial verification (25%)
        if (Boolean.TRUE.equals(financialVerificationStatus)) score += 25;

        // Address verification (15%)
        if (Boolean.TRUE.equals(addressVerificationStatus)) score += 15;

        // Banking verification (20%)
        if (Boolean.TRUE.equals(bankingVerificationStatus)) score += 20;

        this.riskScore = score;

        // Update risk category
        if (score >= 80) {
            this.riskCategory = RiskCategory.LOW;
        } else if (score >= 60) {
            this.riskCategory = RiskCategory.MEDIUM;
        } else if (score >= 40) {
            this.riskCategory = RiskCategory.HIGH;
        } else {
            this.riskCategory = RiskCategory.VERY_HIGH;
        }
    }

    // Helper method to set documents (used by service layer)
    public void setDocuments(List<KycDocument> documents) {
        this.documents = documents != null ? documents : new ArrayList<>();
    }
}