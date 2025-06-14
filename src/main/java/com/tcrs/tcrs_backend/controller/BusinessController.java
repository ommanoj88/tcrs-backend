package com.tcrs.tcrs_backend.controller;


import com.tcrs.tcrs_backend.dto.auth.ApiResponse;
import com.tcrs.tcrs_backend.dto.business.BusinessCreateRequest;
import com.tcrs.tcrs_backend.dto.business.BusinessResponse;
import com.tcrs.tcrs_backend.service.BusinessService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusinessController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);

    @Autowired
    private BusinessService businessService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(
            @Valid @RequestBody BusinessCreateRequest request) {
        logger.info("Business creation request received for GSTIN: {}", request.getGstin());

        BusinessResponse businessResponse = businessService.createBusiness(request);

        ApiResponse<BusinessResponse> response = new ApiResponse<>(
                true,
                "Business profile created successfully",
                businessResponse
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my-businesses")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BusinessResponse>>> getMyBusinesses() {
        logger.info("Get my businesses request received");

        List<BusinessResponse> businesses = businessService.getMyBusinesses();

        ApiResponse<List<BusinessResponse>> response = new ApiResponse<>(
                true,
                "Businesses retrieved successfully",
                businesses
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{businessId}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(@PathVariable Long businessId) {
        logger.info("Get business by ID request received: {}", businessId);

        BusinessResponse business = businessService.getBusinessById(businessId);

        ApiResponse<BusinessResponse> response = new ApiResponse<>(
                true,
                "Business retrieved successfully",
                business
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/gstin/{gstin}")
    @PreAuthorize("hasRole('SME_USER') or hasRole('ADMIN') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessByGstin(@PathVariable String gstin) {
        logger.info("Get business by GSTIN request received: {}", gstin);

        BusinessResponse business = businessService.getBusinessByGstin(gstin);

        ApiResponse<BusinessResponse> response = new ApiResponse<>(
                true,
                "Business retrieved successfully",
                business
        );

        return ResponseEntity.ok(response);
    }
}