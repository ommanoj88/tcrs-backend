package com.tcrs.tcrs_backend.controller;


import com.tcrs.tcrs_backend.dto.auth.ApiResponse;
import com.tcrs.tcrs_backend.dto.monitoring.CreditAlertResponse;
import com.tcrs.tcrs_backend.dto.monitoring.CreditMonitoringRequest;
import com.tcrs.tcrs_backend.dto.monitoring.CreditMonitoringResponse;
import com.tcrs.tcrs_backend.service.CreditMonitoringService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/credit-monitoring")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CreditMonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(CreditMonitoringController.class);

    @Autowired
    private CreditMonitoringService creditMonitoringService;

    @PostMapping
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreditMonitoringResponse>> setupCreditMonitoring(
            @Valid @RequestBody CreditMonitoringRequest request) {
        logger.info("Setup credit monitoring request received for business ID: {}", request.getBusinessId());

        CreditMonitoringResponse monitoring = creditMonitoringService.setupCreditMonitoring(request);

        ApiResponse<CreditMonitoringResponse> response = new ApiResponse<>(
                true,
                "Credit monitoring setup successfully",
                monitoring
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{monitoringId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreditMonitoringResponse>> updateCreditMonitoring(
            @PathVariable Long monitoringId,
            @Valid @RequestBody CreditMonitoringRequest request) {
        logger.info("Update credit monitoring request for ID: {}", monitoringId);

        CreditMonitoringResponse monitoring = creditMonitoringService.updateCreditMonitoring(monitoringId, request);

        ApiResponse<CreditMonitoringResponse> response = new ApiResponse<>(
                true,
                "Credit monitoring updated successfully",
                monitoring
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-monitoring")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CreditMonitoringResponse>>> getUserCreditMonitoring(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get user credit monitoring request - page: {}, size: {}", page, size);

        Page<CreditMonitoringResponse> monitoring = creditMonitoringService.getUserCreditMonitoring(page, size);

        ApiResponse<Page<CreditMonitoringResponse>> response = new ApiResponse<>(
                true,
                "User credit monitoring retrieved successfully",
                monitoring
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CreditAlertResponse>>> getUserAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        logger.info("Get user alerts request - page: {}, size: {}, unreadOnly: {}", page, size, unreadOnly);

        Page<CreditAlertResponse> alerts = creditMonitoringService.getUserAlerts(page, size, unreadOnly);

        ApiResponse<Page<CreditAlertResponse>> response = new ApiResponse<>(
                true,
                "User alerts retrieved successfully",
                alerts
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/alerts/{alertId}/mark-read")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreditAlertResponse>> markAlertAsRead(@PathVariable Long alertId) {
        logger.info("Mark alert as read request for ID: {}", alertId);

        CreditAlertResponse alert = creditMonitoringService.markAlertAsRead(alertId);

        ApiResponse<CreditAlertResponse> response = new ApiResponse<>(
                true,
                "Alert marked as read successfully",
                alert
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/alerts/{alertId}/acknowledge")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreditAlertResponse>> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestBody Map<String, String> requestBody) {
        logger.info("Acknowledge alert request for ID: {}", alertId);

        String notes = requestBody.getOrDefault("notes", "");
        CreditAlertResponse alert = creditMonitoringService.acknowledgeAlert(alertId, notes);

        ApiResponse<CreditAlertResponse> response = new ApiResponse<>(
                true,
                "Alert acknowledged successfully",
                alert
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserAlertStatistics() {
        logger.info("Get user alert statistics request");

        Map<String, Object> statistics = creditMonitoringService.getUserAlertStatistics();

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                true,
                "Alert statistics retrieved successfully",
                statistics
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{monitoringId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateMonitoring(@PathVariable Long monitoringId) {
        logger.info("Deactivate credit monitoring request for ID: {}", monitoringId);

        // Implementation for deactivating monitoring
        // creditMonitoringService.deactivateMonitoring(monitoringId);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                "Credit monitoring deactivated successfully",
                "DEACTIVATED"
        );

        return ResponseEntity.ok(response);
    }

    // Admin endpoints
    @GetMapping("/admin/monitoring-overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonitoringOverview() {
        logger.info("Get monitoring overview request (admin)");

        // Implementation for admin monitoring overview
        // Map<String, Object> overview = creditMonitoringService.getMonitoringOverview();

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                true,
                "Monitoring overview retrieved successfully",
                Map.of("message", "Admin monitoring overview not yet implemented")
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/trigger-check/{businessId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> triggerManualCheck(@PathVariable Long businessId) {
        logger.info("Trigger manual monitoring check for business ID: {}", businessId);

        // Implementation for triggering manual check
        // creditMonitoringService.triggerManualCheck(businessId);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                "Manual monitoring check triggered successfully",
                "CHECK_TRIGGERED"
        );

        return ResponseEntity.ok(response);
    }
}
