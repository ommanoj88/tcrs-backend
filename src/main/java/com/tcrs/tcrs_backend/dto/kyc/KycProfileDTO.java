package com.tcrs.tcrs_backend.dto.kyc;

import com.tcrs.tcrs_backend.entity.KycStatus;
import com.tcrs.tcrs_backend.entity.KycLevel;
import com.tcrs.tcrs_backend.entity.RiskCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycProfileDTO {
    private Long id;
    private Long businessId;
    private String businessName;
    private KycStatus kycStatus;
    private KycLevel kycLevel;
    private Integer completionPercentage;
    private Double riskScore;
    private RiskCategory riskCategory;

    // Verification statuses
    private Boolean businessVerificationStatus;
    private Boolean directorVerificationStatus;
    private Boolean financialVerificationStatus;
    private Boolean addressVerificationStatus;
    private Boolean bankingVerificationStatus;

    private LocalDateTime lastVerificationDate;
    private LocalDateTime nextReviewDate;
    private String assignedOfficer;
    private String verificationNotes;
    private String complianceFlags;

    private List<KycDocumentDTO> documents;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods
    public String getStatusColor() {
        if (kycStatus == null) return "gray";

        switch (kycStatus) {
            case COMPLETED: return "green";
            case IN_PROGRESS: return "blue";
            case PENDING_REVIEW: return "yellow";
            case REJECTED: return "red";
            case EXPIRED: return "orange";
            case ON_HOLD: return "purple";
            default: return "gray";
        }
    }

    public String getRiskColor() {
        if (riskCategory == null) return "gray";

        switch (riskCategory) {
            case LOW: return "green";
            case MEDIUM: return "yellow";
            case HIGH: return "orange";
            case VERY_HIGH: return "red";
            default: return "gray";
        }
    }

    public boolean needsReview() {
        return nextReviewDate != null && LocalDateTime.now().isAfter(nextReviewDate);
    }

    public int getVerifiedDocumentsCount() {
        if (documents == null) return 0;
        return (int) documents.stream()
                .filter(doc -> doc.getVerificationStatus().name().equals("VERIFIED"))
                .count();
    }

    public int getTotalDocumentsCount() {
        return documents != null ? documents.size() : 0;
    }
}