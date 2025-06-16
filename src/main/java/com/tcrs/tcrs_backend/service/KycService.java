package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.kyc.*;
import com.tcrs.tcrs_backend.entity.*;
import com.tcrs.tcrs_backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class KycService {

    private static final Logger logger = LoggerFactory.getLogger(KycService.class);

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    @Autowired
    private KycProfileRepository kycProfileRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Document Upload Methods
    public KycDocumentDTO uploadDocument(MultipartFile file, DocumentUploadRequest request, String uploadedBy) throws IOException {
        try {
            logger.info("Uploading document for business: {}, type: {}", request.getBusinessId(), request.getDocumentType());

            // Validate business exists
            Business business = businessRepository.findById(request.getBusinessId())
                    .orElseThrow(() -> new RuntimeException("Business not found with ID: " + request.getBusinessId()));

            // Store file
            FileStorageService.FileUploadResult uploadResult = fileStorageService.storeFile(file, "kyc-documents");

            // Create KYC document entity
            KycDocument document = new KycDocument();
            document.setBusiness(business);
            document.setDocumentType(request.getDocumentType());
            document.setDocumentNumber(request.getDocumentNumber());
            document.setOriginalFilename(uploadResult.getOriginalFilename());
            document.setStoredFilename(uploadResult.getStoredFilename());
            document.setFilePath(uploadResult.getFilePath());
            document.setFileSize(uploadResult.getFileSize());
            document.setMimeType(uploadResult.getMimeType());
            document.setVerificationStatus(VerificationStatus.PENDING);
            document.setExpiryDate(request.getExpiryDate());
            document.setIsPrimary(request.getIsPrimary());
            document.setUploadedBy(uploadedBy);

            // If this is marked as primary, make sure other documents of same type are not primary
            if (Boolean.TRUE.equals(request.getIsPrimary())) {
                markOtherDocumentsAsNonPrimary(request.getBusinessId(), request.getDocumentType());
            }

            // Save document
            document = kycDocumentRepository.save(document);

            // Update KYC profile
            updateKycProfile(request.getBusinessId());

            logger.info("Document uploaded successfully with ID: {}", document.getId());

            return convertToDocumentDTO(document);

        } catch (Exception e) {
            logger.error("Error uploading document", e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    public List<KycDocumentDTO> getDocumentsByBusiness(Long businessId) {
        List<KycDocument> documents = kycDocumentRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        return documents.stream()
                .map(this::convertToDocumentDTO)
                .collect(Collectors.toList());
    }

    public KycDocumentDTO getDocumentById(Long documentId) {
        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
        return convertToDocumentDTO(document);
    }

    public void deleteDocument(Long documentId, String deletedBy) {
        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        // Delete physical file
        fileStorageService.deleteFile(document.getFilePath());

        // Delete database record
        kycDocumentRepository.delete(document);

        // Update KYC profile
        updateKycProfile(document.getBusiness().getId());

        logger.info("Document deleted by {}: {}", deletedBy, documentId);
    }

    // Document Verification Methods
    public KycDocumentDTO verifyDocument(DocumentVerificationRequest request, String verifiedBy) {
        KycDocument document = kycDocumentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + request.getDocumentId()));

        // Update verification details
        document.setVerificationStatus(request.getVerificationStatus());
        document.setVerificationNotes(request.getVerificationNotes());
        document.setVerifiedBy(verifiedBy);
        document.setVerifiedAt(LocalDateTime.now());
        document.setConfidenceScore(request.getConfidenceScore());

        document = kycDocumentRepository.save(document);

        // Update KYC profile based on verification
        updateKycProfile(document.getBusiness().getId());

        logger.info("Document verified by {}: {} - Status: {}", verifiedBy, document.getId(), request.getVerificationStatus());

        return convertToDocumentDTO(document);
    }

    public List<KycDocumentDTO> getPendingDocuments() {
        List<KycDocument> documents = kycDocumentRepository.findPendingDocuments();
        return documents.stream()
                .map(this::convertToDocumentDTO)
                .collect(Collectors.toList());
    }

    public Page<KycDocumentDTO> getPendingDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<KycDocument> documentPage = kycDocumentRepository.findAll(pageable);
        return documentPage.map(this::convertToDocumentDTO);
    }

    // KYC Profile Methods
    public KycProfileDTO getKycProfile(Long businessId) {
        Optional<KycProfile> profileOpt = kycProfileRepository.findByBusinessId(businessId);

        if (profileOpt.isPresent()) {
            return convertToProfileDTO(profileOpt.get());
        } else {
            // Create new KYC profile if doesn't exist
            return createKycProfile(businessId);
        }
    }

    public KycProfileDTO createKycProfile(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + businessId));

        KycProfile profile = new KycProfile();
        profile.setBusiness(business);
        profile.setKycStatus(KycStatus.NOT_STARTED);
        profile.setKycLevel(KycLevel.BASIC);
        profile.setCompletionPercentage(0);
        profile.setRiskCategory(RiskCategory.LOW);

        profile = kycProfileRepository.save(profile);

        logger.info("KYC profile created for business: {}", businessId);

        return convertToProfileDTO(profile);
    }

    public void updateKycProfile(Long businessId) {
        Optional<KycProfile> profileOpt = kycProfileRepository.findByBusinessId(businessId);

        if (profileOpt.isEmpty()) {
            createKycProfile(businessId);
            return;
        }

        KycProfile profile = profileOpt.get();

        // Update verification statuses based on documents
        updateVerificationStatuses(profile);

        // Update completion percentage and status
        profile.updateCompletionPercentage();

        // Calculate risk score
        profile.calculateRiskScore();

        // Set next review date if completed
        if (profile.isCompleted() && profile.getNextReviewDate() == null) {
            profile.setNextReviewDate(LocalDateTime.now().plusMonths(12)); // Review annually
        }

        kycProfileRepository.save(profile);

        logger.info("KYC profile updated for business: {} - Completion: {}%", businessId, profile.getCompletionPercentage());
    }

    public List<KycProfileDTO> getHighRiskProfiles() {
        List<KycProfile> profiles = kycProfileRepository.findHighRiskProfiles();
        return profiles.stream()
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    public List<KycProfileDTO> getProfilesNeedingReview() {
        List<KycProfile> profiles = kycProfileRepository.findProfilesNeedingReview(LocalDateTime.now());
        return profiles.stream()
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    // Analytics Methods
    public KycAnalyticsDTO getKycAnalytics() {
        KycAnalyticsDTO analytics = new KycAnalyticsDTO();

        // Document statistics
        analytics.setTotalDocuments(kycDocumentRepository.count());
        analytics.setPendingDocuments(kycDocumentRepository.countByVerificationStatus(VerificationStatus.PENDING));
        analytics.setVerifiedDocuments(kycDocumentRepository.countByVerificationStatus(VerificationStatus.VERIFIED));
        analytics.setRejectedDocuments(kycDocumentRepository.countByVerificationStatus(VerificationStatus.REJECTED));

        // Profile statistics
        analytics.setTotalProfiles(kycProfileRepository.count());
        analytics.setCompletedProfiles(kycProfileRepository.countByKycStatus(KycStatus.COMPLETED));
        analytics.setInProgressProfiles(kycProfileRepository.countByKycStatus(KycStatus.IN_PROGRESS));
        analytics.setPendingReviewProfiles(kycProfileRepository.countByKycStatus(KycStatus.PENDING_REVIEW));

        // Risk distribution
        analytics.setLowRiskProfiles(kycProfileRepository.countByRiskCategory(RiskCategory.LOW));
        analytics.setMediumRiskProfiles(kycProfileRepository.countByRiskCategory(RiskCategory.MEDIUM));
        analytics.setHighRiskProfiles(kycProfileRepository.countByRiskCategory(RiskCategory.HIGH));
        analytics.setVeryHighRiskProfiles(kycProfileRepository.countByRiskCategory(RiskCategory.VERY_HIGH));

        // Averages
        analytics.setAverageCompletionPercentage(kycProfileRepository.getAverageCompletionPercentage());
        analytics.setAverageRiskScore(kycProfileRepository.getAverageRiskScore());

        return analytics;
    }

    // Helper Methods
    private void markOtherDocumentsAsNonPrimary(Long businessId, DocumentType documentType) {
        List<KycDocument> existingDocs = kycDocumentRepository.findByBusinessIdAndDocumentType(businessId, documentType);
        for (KycDocument doc : existingDocs) {
            doc.setIsPrimary(false);
        }
        kycDocumentRepository.saveAll(existingDocs);
    }

    private void updateVerificationStatuses(KycProfile profile) {
        Long businessId = profile.getBusiness().getId();

        // Business verification (incorporation, GST, PAN)
        boolean businessVerified = hasVerifiedDocument(businessId, DocumentType.CERTIFICATE_OF_INCORPORATION) ||
                hasVerifiedDocument(businessId, DocumentType.GST_CERTIFICATE) ||
                hasVerifiedDocument(businessId, DocumentType.PAN_CARD);
        profile.setBusinessVerificationStatus(businessVerified);

        // Director verification
        boolean directorVerified = hasVerifiedDocument(businessId, DocumentType.DIRECTOR_PAN) &&
                (hasVerifiedDocument(businessId, DocumentType.DIRECTOR_AADHAAR) ||
                        hasVerifiedDocument(businessId, DocumentType.DIRECTOR_PASSPORT));
        profile.setDirectorVerificationStatus(directorVerified);

        // Financial verification
        boolean financialVerified = hasVerifiedDocument(businessId, DocumentType.BANK_STATEMENT) ||
                hasVerifiedDocument(businessId, DocumentType.FINANCIAL_STATEMENTS);
        profile.setFinancialVerificationStatus(financialVerified);

        // Address verification
        boolean addressVerified = hasVerifiedDocument(businessId, DocumentType.UTILITY_BILL) ||
                hasVerifiedDocument(businessId, DocumentType.RENTAL_AGREEMENT);
        profile.setAddressVerificationStatus(addressVerified);

        // Banking verification
        boolean bankingVerified = hasVerifiedDocument(businessId, DocumentType.CANCELLED_CHEQUE) ||
                hasVerifiedDocument(businessId, DocumentType.BANK_ACCOUNT_CERTIFICATE);
        profile.setBankingVerificationStatus(bankingVerified);
    }

    private boolean hasVerifiedDocument(Long businessId, DocumentType documentType) {
        List<KycDocument> docs = kycDocumentRepository.findByBusinessIdAndDocumentType(businessId, documentType);
        return docs.stream().anyMatch(doc -> doc.getVerificationStatus() == VerificationStatus.VERIFIED);
    }

    private KycDocumentDTO convertToDocumentDTO(KycDocument document) {
        KycDocumentDTO dto = new KycDocumentDTO();
        dto.setId(document.getId());
        dto.setBusinessId(document.getBusiness().getId());
        dto.setBusinessName(document.getBusiness().getBusinessName());
        dto.setDocumentType(document.getDocumentType());
        dto.setDocumentNumber(document.getDocumentNumber());
        dto.setOriginalFilename(document.getOriginalFilename());
        dto.setFileSize(String.valueOf(document.getFileSize()));
        dto.setMimeType(document.getMimeType());
        dto.setVerificationStatus(document.getVerificationStatus());
        dto.setVerificationNotes(document.getVerificationNotes());
        dto.setVerifiedBy(document.getVerifiedBy());
        dto.setVerifiedAt(document.getVerifiedAt());
        dto.setExpiryDate(document.getExpiryDate());
        dto.setIsPrimary(document.getIsPrimary());
        dto.setConfidenceScore(document.getConfidenceScore());
        dto.setUploadedBy(document.getUploadedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        return dto;
    }

    private KycProfileDTO convertToProfileDTO(KycProfile profile) {
        KycProfileDTO dto = new KycProfileDTO();
        dto.setId(profile.getId());
        dto.setBusinessId(profile.getBusiness().getId());
        dto.setBusinessName(profile.getBusiness().getBusinessName());
        dto.setKycStatus(profile.getKycStatus());
        dto.setKycLevel(profile.getKycLevel());
        dto.setCompletionPercentage(profile.getCompletionPercentage());
        dto.setRiskScore(profile.getRiskScore());
        dto.setRiskCategory(profile.getRiskCategory());
        dto.setBusinessVerificationStatus(profile.getBusinessVerificationStatus());
        dto.setDirectorVerificationStatus(profile.getDirectorVerificationStatus());
        dto.setFinancialVerificationStatus(profile.getFinancialVerificationStatus());
        dto.setAddressVerificationStatus(profile.getAddressVerificationStatus());
        dto.setBankingVerificationStatus(profile.getBankingVerificationStatus());
        dto.setLastVerificationDate(profile.getLastVerificationDate());
        dto.setNextReviewDate(profile.getNextReviewDate());
        dto.setAssignedOfficer(profile.getAssignedOfficer());
        dto.setVerificationNotes(profile.getVerificationNotes());
        dto.setComplianceFlags(profile.getComplianceFlags());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());

        // Load documents
        List<KycDocument> documents = kycDocumentRepository.findByBusinessIdOrderByCreatedAtDesc(profile.getBusiness().getId());
        dto.setDocuments(documents.stream().map(this::convertToDocumentDTO).collect(Collectors.toList()));

        return dto;
    }
}