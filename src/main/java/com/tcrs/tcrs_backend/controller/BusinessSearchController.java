package com.tcrs.tcrs_backend.controller;

import com.tcrs.tcrs_backend.dto.auth.ApiResponse;
import com.tcrs.tcrs_backend.dto.business.BusinessResponse;
import com.tcrs.tcrs_backend.dto.business.BusinessSearchRequest;
import com.tcrs.tcrs_backend.dto.business.BusinessSearchResponse;
import com.tcrs.tcrs_backend.entity.IndustryCategory;
import com.tcrs.tcrs_backend.service.BusinessSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusinessSearchController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessSearchController.class);

    @Autowired
    private BusinessSearchService businessSearchService;

    @PostMapping("/businesses")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<BusinessSearchResponse>> searchBusinesses(
            @RequestBody BusinessSearchRequest request) {
        logger.info("Business search request received with query: {}", request.getQuery());

        BusinessSearchResponse searchResponse = businessSearchService.searchBusinesses(request);

        ApiResponse<BusinessSearchResponse> response = new ApiResponse<>(
                true,
                "Businesses retrieved successfully",
                searchResponse
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/businesses/gstin/{gstin}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessByGstin(@PathVariable String gstin) {
        logger.info("Get business by GSTIN request: {}", gstin);

        BusinessResponse business = businessSearchService.getBusinessDetails(gstin);

        ApiResponse<BusinessResponse> response = new ApiResponse<>(
                true,
                "Business details retrieved successfully",
                business
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/businesses/{businessId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(@PathVariable Long businessId) {
        logger.info("Get business by ID request: {}", businessId);

        BusinessResponse business = businessSearchService.getBusinessDetails(businessId);

        ApiResponse<BusinessResponse> response = new ApiResponse<>(
                true,
                "Business details retrieved successfully",
                business
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/businesses/location/{location}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<BusinessResponse>>> getBusinessesByLocation(
            @PathVariable String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get businesses by location request: {}", location);

        List<BusinessResponse> businesses = businessSearchService.getBusinessesByLocation(location, page, size);

        ApiResponse<List<BusinessResponse>> response = new ApiResponse<>(
                true,
                "Businesses retrieved successfully",
                businesses
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/businesses/industry/{industry}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<BusinessResponse>>> getBusinessesByIndustry(
            @PathVariable IndustryCategory industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Get businesses by industry request: {}", industry);

        List<BusinessResponse> businesses = businessSearchService.getBusinessesByIndustry(industry, page, size);

        ApiResponse<List<BusinessResponse>> response = new ApiResponse<>(
                true,
                "Businesses retrieved successfully",
                businesses
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Long>> getTotalBusinesses() {
        logger.info("Get total businesses stats request");

        long totalBusinesses = businessSearchService.getTotalActiveBusinesses();

        ApiResponse<Long> response = new ApiResponse<>(
                true,
                "Business statistics retrieved successfully",
                totalBusinesses
        );

        return ResponseEntity.ok(response);
    }
}