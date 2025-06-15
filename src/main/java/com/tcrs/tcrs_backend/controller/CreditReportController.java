package com.tcrs.tcrs_backend.controller;

import com.tcrs.tcrs_backend.dto.auth.ApiResponse;
import com.tcrs.tcrs_backend.dto.credit.CreditReportRequest;
import com.tcrs.tcrs_backend.dto.credit.CreditReportResponse;
import com.tcrs.tcrs_backend.service.CreditReportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CreditReportController {

    private static final Logger logger = LoggerFactory.getLogger(CreditReportController.class);

    @Autowired
    private CreditReportService creditReportService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<CreditReportResponse>> generateCreditReport(
            @Valid @RequestBody CreditReportRequest request) {
        logger.info("Generate credit report request received for business ID: {}", request.getBusinessId());

        CreditReportResponse creditReport = creditReportService.generateCreditReport(request);

        ApiResponse<CreditReportResponse> response = new ApiResponse<>(
                true,
                "Credit report generated successfully",
                creditReport
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/{reportNumber}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<CreditReportResponse>> getCreditReport(@PathVariable String reportNumber) {
        logger.info("Get credit report request for report number: {}", reportNumber);

        CreditReportResponse creditReport = creditReportService.getCreditReport(reportNumber);

        ApiResponse<CreditReportResponse> response = new ApiResponse<>(
                true,
                "Credit report retrieved successfully",
                creditReport
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/id/{reportId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<CreditReportResponse>> getCreditReportById(@PathVariable Long reportId) {
        logger.info("Get credit report request for report ID: {}", reportId);

        CreditReportResponse creditReport = creditReportService.getCreditReportById(reportId);

        ApiResponse<CreditReportResponse> response = new ApiResponse<>(
                true,
                "Credit report retrieved successfully",
                creditReport
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}/history")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<CreditReportResponse>>> getBusinessCreditHistory(
            @PathVariable Long businessId) {
        logger.info("Get credit history request for business ID: {}", businessId);

        List<CreditReportResponse> creditHistory = creditReportService.getBusinessCreditHistory(businessId);

        ApiResponse<List<CreditReportResponse>> response = new ApiResponse<>(
                true,
                "Business credit history retrieved successfully",
                creditHistory
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-reports")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<CreditReportResponse>>> getUserCreditReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get user credit reports request - page: {}, size: {}", page, size);

        Page<CreditReportResponse> creditReports = creditReportService.getUserCreditReports(page, size);

        ApiResponse<Page<CreditReportResponse>> response = new ApiResponse<>(
                true,
                "User credit reports retrieved successfully",
                creditReports
        );

        return ResponseEntity.ok(response);
    }
}