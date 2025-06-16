package com.tcrs.tcrs_backend.dto.kyc;

import com.tcrs.tcrs_backend.entity.DocumentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    private LocalDateTime expiryDate;

    private Boolean isPrimary = false;

    private String notes;
}