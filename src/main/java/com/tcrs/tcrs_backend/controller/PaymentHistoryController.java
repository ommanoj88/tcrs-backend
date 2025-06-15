package com.tcrs.tcrs_backend.controller;


import com.tcrs.tcrs_backend.dto.auth.ApiResponse;
import com.tcrs.tcrs_backend.dto.payment.PaymentAnalyticsResponse;
import com.tcrs.tcrs_backend.dto.payment.PaymentHistoryRequest;
import com.tcrs.tcrs_backend.dto.payment.PaymentHistoryResponse;
import com.tcrs.tcrs_backend.service.PaymentHistoryService;
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
@RequestMapping("/api/payment-history")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentHistoryController.class);

    @Autowired
    private PaymentHistoryService paymentHistoryService;

    @PostMapping
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentHistoryResponse>> addPaymentHistory(
            @Valid @RequestBody PaymentHistoryRequest request) {
        logger.info("Add payment history request received for business ID: {}", request.getBusinessId());

        PaymentHistoryResponse paymentHistory = paymentHistoryService.addPaymentHistory(request);

        ApiResponse<PaymentHistoryResponse> response = new ApiResponse<>(
                true,
                "Payment history added successfully",
                paymentHistory
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{paymentHistoryId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentHistoryResponse>> updatePaymentHistory(
            @PathVariable Long paymentHistoryId,
            @Valid @RequestBody PaymentHistoryRequest request) {
        logger.info("Update payment history request for ID: {}", paymentHistoryId);

        PaymentHistoryResponse paymentHistory = paymentHistoryService.updatePaymentHistory(paymentHistoryId, request);

        ApiResponse<PaymentHistoryResponse> response = new ApiResponse<>(
                true,
                "Payment history updated successfully",
                paymentHistory
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentHistoryId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<PaymentHistoryResponse>> getPaymentHistory(@PathVariable Long paymentHistoryId) {
        logger.info("Get payment history request for ID: {}", paymentHistoryId);

        PaymentHistoryResponse paymentHistory = paymentHistoryService.getPaymentHistory(paymentHistoryId);

        ApiResponse<PaymentHistoryResponse> response = new ApiResponse<>(
                true,
                "Payment history retrieved successfully",
                paymentHistory
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getBusinessPaymentHistory(
            @PathVariable Long businessId) {
        logger.info("Get business payment history request for business ID: {}", businessId);

        List<PaymentHistoryResponse> paymentHistories = paymentHistoryService.getBusinessPaymentHistory(businessId);

        ApiResponse<List<PaymentHistoryResponse>> response = new ApiResponse<>(
                true,
                "Business payment history retrieved successfully",
                paymentHistories
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}/paginated")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<PaymentHistoryResponse>>> getBusinessPaymentHistoryPaginated(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get paginated business payment history for business ID: {} - page: {}, size: {}",
                businessId, page, size);

        Page<PaymentHistoryResponse> paymentHistories = paymentHistoryService
                .getBusinessPaymentHistoryPaginated(businessId, page, size);

        ApiResponse<Page<PaymentHistoryResponse>> response = new ApiResponse<>(
                true,
                "Business payment history retrieved successfully",
                paymentHistories
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-reports")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentHistoryResponse>>> getUserPaymentHistories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get user payment histories request - page: {}, size: {}", page, size);

        Page<PaymentHistoryResponse> paymentHistories = paymentHistoryService
                .getUserPaymentHistories(page, size);

        ApiResponse<Page<PaymentHistoryResponse>> response = new ApiResponse<>(
                true,
                "User payment histories retrieved successfully",
                paymentHistories
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}/analytics")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<PaymentAnalyticsResponse>> getPaymentAnalytics(@PathVariable Long businessId) {
        logger.info("Get payment analytics request for business ID: {}", businessId);

        PaymentAnalyticsResponse analytics = paymentHistoryService.getPaymentAnalytics(businessId);

        ApiResponse<PaymentAnalyticsResponse> response = new ApiResponse<>(
                true,
                "Payment analytics retrieved successfully",
                analytics
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentHistoryId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> verifyPaymentHistory(
            @PathVariable Long paymentHistoryId,
            @RequestBody Map<String, Object> verificationRequest) {
        logger.info("Verify payment history request for ID: {}", paymentHistoryId);

        boolean approve = (Boolean) verificationRequest.get("approve");
        String reason = (String) verificationRequest.getOrDefault("reason", "");

        paymentHistoryService.verifyPaymentHistory(paymentHistoryId, approve, reason);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                approve ? "Payment history verified successfully" : "Payment history rejected",
                approve ? "VERIFIED" : "REJECTED"
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{paymentHistoryId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deletePaymentHistory(@PathVariable Long paymentHistoryId) {
        logger.info("Delete payment history request for ID: {}", paymentHistoryId);

        paymentHistoryService.deletePaymentHistory(paymentHistoryId);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                "Payment history deleted successfully",
                "DELETED"
        );

        return ResponseEntity.ok(response);
    }
}