package com.tcrs.tcrs_backend.controller;


import com.tcrs.tcrs_backend.dto.analytics.DashboardAnalyticsDTO;
import com.tcrs.tcrs_backend.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardAnalytics() {
        try {
            logger.info("Fetching dashboard analytics");

            DashboardAnalyticsDTO analytics = analyticsService.getDashboardAnalytics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", analytics);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching dashboard analytics", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to load analytics: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/business-metrics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBusinessMetrics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String location) {
        try {
            // Implementation for filtered business metrics
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Business metrics endpoint - to be implemented");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching business metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/credit-trends")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCreditTrends(
            @RequestParam(required = false, defaultValue = "12") int months) {
        try {
            // Implementation for credit trends
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Credit trends endpoint - to be implemented");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching credit trends", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/payment-analysis")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentAnalysis(
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String riskLevel) {
        try {
            // Implementation for payment analysis
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment analysis endpoint - to be implemented");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching payment analysis", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> exportAnalytics(
            @RequestParam String format,
            @RequestParam String reportType) {
        try {
            // Implementation for exporting analytics
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Export functionality - to be implemented");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error exporting analytics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}