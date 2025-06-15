package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.business.BusinessResponse;
import com.tcrs.tcrs_backend.dto.business.BusinessSearchRequest;
import com.tcrs.tcrs_backend.dto.business.BusinessSearchResponse;
import com.tcrs.tcrs_backend.entity.Business;
import com.tcrs.tcrs_backend.entity.BusinessType;
import com.tcrs.tcrs_backend.entity.IndustryCategory;
import com.tcrs.tcrs_backend.exception.ResourceNotFoundException;
import com.tcrs.tcrs_backend.repository.BusinessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BusinessSearchService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessSearchService.class);

    @Autowired
    private BusinessRepository businessRepository;

    public BusinessSearchResponse searchBusinesses(BusinessSearchRequest request) {
        logger.info("Searching businesses with query: {}", request.getQuery());

        // Create pageable
        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Business> businessPage;

        // Determine search type
        if (hasAdvancedFilters(request)) {
            businessPage = businessRepository.advancedSearchBusinesses(
                    request.getBusinessName(),
                    request.getGstin(),
                    request.getPan(),
                    request.getCity(),
                    request.getState(),
                    request.getBusinessType(),
                    request.getIndustryCategory(),
                    pageable
            );
        } else if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
            businessPage = businessRepository.searchBusinesses(request.getQuery().trim(), pageable);
        } else {
            // Return all active businesses if no search criteria
            businessPage = businessRepository.findRecentBusinesses(pageable);
        }

        // Convert to response
        List<BusinessResponse> businessResponses = businessPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        logger.info("Found {} businesses matching search criteria", businessPage.getTotalElements());

        return new BusinessSearchResponse(
                businessResponses,
                businessPage.getNumber(),
                businessPage.getTotalPages(),
                businessPage.getTotalElements(),
                businessPage.hasNext(),
                businessPage.hasPrevious()
        );
    }

    public BusinessResponse getBusinessDetails(String gstin) {
        logger.info("Getting business details for GSTIN: {}", gstin);

        Business business = businessRepository.findActiveBusinessByGstin(gstin)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with GSTIN: " + gstin));

        return convertToResponse(business);
    }

    public BusinessResponse getBusinessDetails(Long businessId) {
        logger.info("Getting business details for ID: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        if (!business.getIsActive()) {
            throw new ResourceNotFoundException("Business is not active");
        }

        return convertToResponse(business);
    }

    public List<BusinessResponse> getBusinessesByLocation(String location, int page, int size) {
        logger.info("Getting businesses by location: {}", location);

        Pageable pageable = PageRequest.of(page, size, Sort.by("businessName"));
        Page<Business> businessPage = businessRepository
                .findByIsActiveTrueAndCityContainingIgnoreCaseOrStateContainingIgnoreCase(
                        location, location, pageable);

        return businessPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<BusinessResponse> getBusinessesByIndustry(IndustryCategory industry, int page, int size) {
        logger.info("Getting businesses by industry: {}", industry);

        Pageable pageable = PageRequest.of(page, size, Sort.by("businessName"));
        Page<Business> businessPage = businessRepository
                .findByIsActiveTrueAndIndustryCategory(industry, pageable);

        return businessPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public long getTotalActiveBusinesses() {
        return businessRepository.countByIsActiveTrue();
    }

    private boolean hasAdvancedFilters(BusinessSearchRequest request) {
        return (request.getBusinessName() != null && !request.getBusinessName().trim().isEmpty()) ||
                (request.getGstin() != null && !request.getGstin().trim().isEmpty()) ||
                (request.getPan() != null && !request.getPan().trim().isEmpty()) ||
                (request.getCity() != null && !request.getCity().trim().isEmpty()) ||
                (request.getState() != null && !request.getState().trim().isEmpty()) ||
                request.getBusinessType() != null ||
                request.getIndustryCategory() != null;
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Validate sort field
        String validatedSortBy = switch (sortBy) {
            case "businessName", "createdAt", "city", "state" -> sortBy;
            default -> "businessName";
        };

        return Sort.by(direction, validatedSortBy);
    }

    private BusinessResponse convertToResponse(Business business) {
        BusinessResponse response = new BusinessResponse();
        response.setId(business.getId());
        response.setBusinessName(business.getBusinessName());
        response.setGstin(business.getGstin());
        response.setPan(business.getPan());
        response.setBusinessType(business.getBusinessType());
        response.setIndustryCategory(business.getIndustryCategory());
        response.setRegistrationDate(business.getRegistrationDate());
        response.setBusinessDescription(business.getBusinessDescription());
        response.setRegisteredAddress(business.getRegisteredAddress());
        response.setCity(business.getCity());
        response.setState(business.getState());
        response.setPincode(business.getPincode());
        response.setBusinessPhone(business.getBusinessPhone());
        response.setBusinessEmail(business.getBusinessEmail());
        response.setWebsite(business.getWebsite());
        response.setGstinVerified(business.getGstinVerified());
        response.setPanVerified(business.getPanVerified());
        response.setIsActive(business.getIsActive());
        response.setOwnerId(business.getOwner().getId());
        response.setOwnerName(business.getOwner().getFirstName() + " " + business.getOwner().getLastName());
        response.setCreatedAt(business.getCreatedAt());
        response.setUpdatedAt(business.getUpdatedAt());

        return response;
    }
}
