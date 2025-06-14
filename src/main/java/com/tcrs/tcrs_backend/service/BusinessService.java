package com.tcrs.tcrs_backend.service;

import com.tcrs.tcrs_backend.dto.business.BusinessCreateRequest;
import com.tcrs.tcrs_backend.dto.business.BusinessResponse;
import com.tcrs.tcrs_backend.entity.Business;
import com.tcrs.tcrs_backend.entity.User;
import com.tcrs.tcrs_backend.exception.BadRequestException;
import com.tcrs.tcrs_backend.exception.ResourceNotFoundException;
import com.tcrs.tcrs_backend.repository.BusinessRepository;
import com.tcrs.tcrs_backend.repository.UserRepository;
import com.tcrs.tcrs_backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    public BusinessResponse createBusiness(BusinessCreateRequest request) {
        logger.info("Creating business profile for GSTIN: {}", request.getGstin());

        // Get current user
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User owner = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if GSTIN already exists
        if (businessRepository.existsByGstin(request.getGstin())) {
            throw new BadRequestException("Business with this GSTIN already exists");
        }

        // Check if PAN already exists
        if (businessRepository.existsByPan(request.getPan())) {
            throw new BadRequestException("Business with this PAN already exists");
        }

        // Create business entity
        Business business = new Business();
        business.setBusinessName(request.getBusinessName());
        business.setGstin(request.getGstin());
        business.setPan(request.getPan());
        business.setBusinessType(request.getBusinessType());
        business.setIndustryCategory(request.getIndustryCategory());
        business.setRegistrationDate(request.getRegistrationDate());
        business.setBusinessDescription(request.getBusinessDescription());
        business.setRegisteredAddress(request.getRegisteredAddress());
        business.setCity(request.getCity());
        business.setState(request.getState());
        business.setPincode(request.getPincode());
        business.setBusinessPhone(request.getBusinessPhone());
        business.setBusinessEmail(request.getBusinessEmail());
        business.setWebsite(request.getWebsite());
        business.setOwner(owner);
        business.setIsActive(true);
        business.setGstinVerified(false);
        business.setPanVerified(false);

        Business savedBusiness = businessRepository.save(business);
        logger.info("Business profile created successfully with ID: {}", savedBusiness.getId());

        return convertToResponse(savedBusiness);
    }

    public List<BusinessResponse> getMyBusinesses() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User owner = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Business> businesses = businessRepository.findActiveBusinessesByOwner(owner);

        return businesses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BusinessResponse getBusinessById(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        // Check if current user owns this business
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!business.getOwner().getId().equals(userPrincipal.getId())) {
            throw new BadRequestException("You don't have permission to access this business");
        }

        return convertToResponse(business);
    }

    public BusinessResponse getBusinessByGstin(String gstin) {
        Business business = businessRepository.findActiveBusinessByGstin(gstin)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with GSTIN: " + gstin));

        return convertToResponse(business);
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
        response.setOwnerName(business.getOwner().getFullName());
        response.setCreatedAt(business.getCreatedAt());
        response.setUpdatedAt(business.getUpdatedAt());

        return response;
    }
}