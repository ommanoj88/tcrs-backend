package com.tcrs.tcrs_backend.dto.kyc;

import com.tcrs.tcrs_backend.entity.VerificationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVerificationRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;

    private String verificationNotes;

    private Double confidenceScore;
}