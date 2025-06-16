package com.tcrs.tcrs_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "extracted_data", columnDefinition = "TEXT")
    private String extractedData; // JSON data extracted from document

    @Column(name = "confidence_score")
    private Double confidenceScore; // AI/ML confidence score (0-1)

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

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
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean isPending() {
        return verificationStatus == VerificationStatus.PENDING;
    }

    public boolean isRejected() {
        return verificationStatus == VerificationStatus.REJECTED;
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    public void approve(String verifiedBy, String notes) {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = LocalDateTime.now();
        this.verificationNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String verifiedBy, String notes) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = LocalDateTime.now();
        this.verificationNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }
}