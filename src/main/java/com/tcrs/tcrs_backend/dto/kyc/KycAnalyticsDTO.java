package com.tcrs.tcrs_backend.dto.kyc;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycAnalyticsDTO {
    // Document Statistics
    private Long totalDocuments;
    private Long pendingDocuments;
    private Long verifiedDocuments;
    private Long rejectedDocuments;

    // Profile Statistics
    private Long totalProfiles;
    private Long completedProfiles;
    private Long inProgressProfiles;
    private Long pendingReviewProfiles;

    // Risk Distribution
    private Long lowRiskProfiles;
    private Long mediumRiskProfiles;
    private Long highRiskProfiles;
    private Long veryHighRiskProfiles;

    // Averages
    private Double averageCompletionPercentage;
    private Double averageRiskScore;

    // Helper methods
    public double getDocumentVerificationRate() {
        if (totalDocuments == null || totalDocuments == 0) return 0.0;
        return (verifiedDocuments != null ? verifiedDocuments : 0) * 100.0 / totalDocuments;
    }

    public double getProfileCompletionRate() {
        if (totalProfiles == null || totalProfiles == 0) return 0.0;
        return (completedProfiles != null ? completedProfiles : 0) * 100.0 / totalProfiles;
    }

    public double getHighRiskPercentage() {
        if (totalProfiles == null || totalProfiles == 0) return 0.0;
        long highRisk = (highRiskProfiles != null ? highRiskProfiles : 0) +
                (veryHighRiskProfiles != null ? veryHighRiskProfiles : 0);
        return highRisk * 100.0 / totalProfiles;
    }
}