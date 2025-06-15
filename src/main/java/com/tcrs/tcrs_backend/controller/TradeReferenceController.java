package com.tcrs.tcrs_backend.controller;


import com.tcrs.tcrs_backend.dto.auth.ApiResponse;
import com.tcrs.tcrs_backend.dto.reference.TradeReferenceAnalyticsResponse;
import com.tcrs.tcrs_backend.dto.reference.TradeReferenceRequest;
import com.tcrs.tcrs_backend.dto.reference.TradeReferenceResponse;
import com.tcrs.tcrs_backend.entity.ReferenceVerificationStatus;
import com.tcrs.tcrs_backend.entity.VerificationMethod;
import com.tcrs.tcrs_backend.service.TradeReferenceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trade-references")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TradeReferenceController {

    private static final Logger logger = LoggerFactory.getLogger(TradeReferenceController.class);

    @Autowired
    private TradeReferenceService tradeReferenceService;

    @PostMapping
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeReferenceResponse>> addTradeReference(
            @Valid @RequestBody TradeReferenceRequest request) {
        logger.info("Add trade reference request received for business ID: {}", request.getBusinessId());

        TradeReferenceResponse tradeReference = tradeReferenceService.addTradeReference(request);

        ApiResponse<TradeReferenceResponse> response = new ApiResponse<>(
                true,
                "Trade reference added successfully",
                tradeReference
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{referenceId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeReferenceResponse>> updateTradeReference(
            @PathVariable Long referenceId,
            @Valid @RequestBody TradeReferenceRequest request) {
        logger.info("Update trade reference request for ID: {}", referenceId);

        TradeReferenceResponse tradeReference = tradeReferenceService.updateTradeReference(referenceId, request);

        ApiResponse<TradeReferenceResponse> response = new ApiResponse<>(
                true,
                "Trade reference updated successfully",
                tradeReference
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{referenceId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<TradeReferenceResponse>> getTradeReference(@PathVariable Long referenceId) {
        logger.info("Get trade reference request for ID: {}", referenceId);

        TradeReferenceResponse tradeReference = tradeReferenceService.getTradeReference(referenceId);

        ApiResponse<TradeReferenceResponse> response = new ApiResponse<>(
                true,
                "Trade reference retrieved successfully",
                tradeReference
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<TradeReferenceResponse>>> getBusinessTradeReferences(
            @PathVariable Long businessId) {
        logger.info("Get business trade references request for business ID: {}", businessId);

        List<TradeReferenceResponse> tradeReferences = tradeReferenceService.getBusinessTradeReferences(businessId);

        ApiResponse<List<TradeReferenceResponse>> response = new ApiResponse<>(
                true,
                "Business trade references retrieved successfully",
                tradeReferences
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}/paginated")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<TradeReferenceResponse>>> getBusinessTradeReferencesPaginated(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get paginated business trade references for business ID: {} - page: {}, size: {}",
                businessId, page, size);

        Page<TradeReferenceResponse> tradeReferences = tradeReferenceService
                .getBusinessTradeReferencesPaginated(businessId, page, size);

        ApiResponse<Page<TradeReferenceResponse>> response = new ApiResponse<>(
                true,
                "Business trade references retrieved successfully",
                tradeReferences
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-references")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TradeReferenceResponse>>> getUserTradeReferences(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get user trade references request - page: {}, size: {}", page, size);

        Page<TradeReferenceResponse> tradeReferences = tradeReferenceService
                .getUserTradeReferences(page, size);

        ApiResponse<Page<TradeReferenceResponse>> response = new ApiResponse<>(
                true,
                "User trade references retrieved successfully",
                tradeReferences
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-verifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TradeReferenceResponse>>> getPendingVerifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get pending verifications request - page: {}, size: {}", page, size);

        Page<TradeReferenceResponse> tradeReferences = tradeReferenceService
                .getPendingVerifications(page, size);

        ApiResponse<Page<TradeReferenceResponse>> response = new ApiResponse<>(
                true,
                "Pending verifications retrieved successfully",
                tradeReferences
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}/analytics")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<TradeReferenceAnalyticsResponse>> getTradeReferenceAnalytics(
            @PathVariable Long businessId) {
        logger.info("Get trade reference analytics request for business ID: {}", businessId);

        TradeReferenceAnalyticsResponse analytics = tradeReferenceService.getTradeReferenceAnalytics(businessId);

        ApiResponse<TradeReferenceAnalyticsResponse> response = new ApiResponse<>(
                true,
                "Trade reference analytics retrieved successfully",
                analytics
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{referenceId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> verifyTradeReference(
            @PathVariable Long referenceId,
            @RequestBody Map<String, Object> verificationRequest) {
        logger.info("Verify trade reference request for ID: {}", referenceId);

        ReferenceVerificationStatus status = ReferenceVerificationStatus.valueOf(
                (String) verificationRequest.get("status"));
        VerificationMethod method = VerificationMethod.valueOf(
                (String) verificationRequest.get("method"));
        String notes = (String) verificationRequest.getOrDefault("notes", "");
        String response = (String) verificationRequest.getOrDefault("response", "");

        tradeReferenceService.verifyTradeReference(referenceId, status, method, notes, response);

        ApiResponse<String> apiResponse = new ApiResponse<>(
                true,
                "Trade reference verification completed successfully",
                "VERIFIED"
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{referenceId}/contact-attempted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> markContactAttempted(
            @PathVariable Long referenceId,
            @RequestBody Map<String, String> requestBody) {
        logger.info("Mark contact attempted for trade reference ID: {}", referenceId);

        String notes = requestBody.getOrDefault("notes", "");

        tradeReferenceService.markContactAttempted(referenceId, notes);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                "Contact attempt marked successfully",
                "CONTACT_ATTEMPTED"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}/search")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<TradeReferenceResponse>>> searchTradeReferences(
            @PathVariable Long businessId,
            @RequestParam String q) {
        logger.info("Search trade references for business ID: {} with term: {}", businessId, q);

        List<TradeReferenceResponse> tradeReferences = tradeReferenceService
                .searchTradeReferences(businessId, q);

        ApiResponse<List<TradeReferenceResponse>> response = new ApiResponse<>(
                true,
                "Trade references search completed successfully",
                tradeReferences
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{referenceId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteTradeReference(@PathVariable Long referenceId) {
        logger.info("Delete trade reference request for ID: {}", referenceId);

        tradeReferenceService.deleteTradeReference(referenceId);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                "Trade reference deleted successfully",
                "DELETED"
        );

        return ResponseEntity.ok(response);
    }
}
