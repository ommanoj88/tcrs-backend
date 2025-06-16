package com.tcrs.tcrs_backend.dto.kyc;

import com.tcrs.tcrs_backend.entity.DocumentType;
import com.tcrs.tcrs_backend.entity.VerificationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycDocumentDTO {
    private Long id;
    private Long businessId;
    private String businessName;
    private DocumentType documentType;
    private String documentNumber;
    private String originalFilename;
    private String fileSize;
    private String mimeType;
    private VerificationStatus verificationStatus;
    private String verificationNotes;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private LocalDateTime expiryDate;
    private Boolean isPrimary;
    private Double confidenceScore;
    private String uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";

        try {
            long size = Long.parseLong(fileSize);
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        } catch (NumberFormatException e) {
            return fileSize;
        }
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
        return expiryDate.isBefore(thirtyDaysFromNow) && !isExpired();
    }
}