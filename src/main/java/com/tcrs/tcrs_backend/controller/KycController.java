package com.tcrs.tcrs_backend.controller;

import com.tcrs.tcrs_backend.dto.kyc.*;
import com.tcrs.tcrs_backend.service.KycService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin(origins = "*")
public class KycController {

    private static final Logger logger = LoggerFactory.getLogger(KycController.class);

    @Autowired
    private KycService kycService;

    // Document Upload Endpoints
    @PostMapping("/documents/upload")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("businessId") Long businessId,
            @RequestParam("documentType") String documentType,
            @RequestParam("documentNumber") String documentNumber,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "isPrimary", required = false, defaultValue = "false") Boolean isPrimary,
            Authentication authentication) {

        try {
            logger.info("Document upload request for business: {}, type: {}", businessId, documentType);

            // Create upload request
            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setBusinessId(businessId);
            request.setDocumentType(com.tcrs.tcrs_backend.entity.DocumentType.valueOf(documentType));
            request.setDocumentNumber(documentNumber);
            request.setIsPrimary(isPrimary);

            // Parse expiry date if provided
            if (expiryDate != null && !expiryDate.isEmpty()) {
                request.setExpiryDate(java.time.LocalDateTime.parse(expiryDate));
            }

            // Upload document
            KycDocumentDTO documentDTO = kycService.uploadDocument(file, request, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("data", documentDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading document", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to upload document: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/documents/business/{businessId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDocumentsByBusiness(@PathVariable Long businessId) {
        try {
            List<KycDocumentDTO> documents = kycService.getDocumentsByBusiness(businessId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", documents);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching documents for business: {}", businessId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch documents: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDocument(@PathVariable Long documentId) {
        try {
            KycDocumentDTO document = kycService.getDocumentById(documentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", document);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching document: {}", documentId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch document: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long documentId, Authentication authentication) {
        try {
            kycService.deleteDocument(documentId, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting document: {}", documentId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete document: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Document Verification Endpoints (Admin only)
    @PostMapping("/documents/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> verifyDocument(
            @Valid @RequestBody DocumentVerificationRequest request,
            Authentication authentication) {

        try {
            KycDocumentDTO document = kycService.verifyDocument(request, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document verification updated successfully");
            response.put("data", document);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error verifying document", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to verify document: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/documents/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Page<KycDocumentDTO> documents = kycService.getPendingDocuments(page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", documents.getContent());
            response.put("totalElements", documents.getTotalElements());
            response.put("totalPages", documents.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching pending documents", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch pending documents: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // KYC Profile Endpoints
    @GetMapping("/profile/{businessId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getKycProfile(@PathVariable Long businessId) {
        try {
            KycProfileDTO profile = kycService.getKycProfile(businessId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profile);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching KYC profile for business: {}", businessId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch KYC profile: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/profile/{businessId}/refresh")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> refreshKycProfile(@PathVariable Long businessId) {
        try {
            kycService.updateKycProfile(businessId);
            KycProfileDTO profile = kycService.getKycProfile(businessId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "KYC profile refreshed successfully");
            response.put("data", profile);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error refreshing KYC profile for business: {}", businessId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to refresh KYC profile: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Admin Analytics & Management Endpoints
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getKycAnalytics() {
        try {
            KycAnalyticsDTO analytics = kycService.getKycAnalytics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", analytics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching KYC analytics", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch KYC analytics: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/profiles/high-risk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getHighRiskProfiles() {
        try {
            List<KycProfileDTO> profiles = kycService.getHighRiskProfiles();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profiles);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching high-risk profiles", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch high-risk profiles: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/profiles/review-needed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProfilesNeedingReview() {
        try {
            List<KycProfileDTO> profiles = kycService.getProfilesNeedingReview();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profiles);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching profiles needing review", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch profiles needing review: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}