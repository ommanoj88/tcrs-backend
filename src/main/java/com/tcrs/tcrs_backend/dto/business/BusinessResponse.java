package com.tcrs.tcrs_backend.dto.business;

import com.tcrs.tcrs_backend.entity.BusinessType;
import com.tcrs.tcrs_backend.entity.IndustryCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BusinessResponse {

    private Long id;
    private String businessName;
    private String gstin;
    private String pan;
    private BusinessType businessType;
    private IndustryCategory industryCategory;
    private LocalDate registrationDate;
    private String businessDescription;
    private String registeredAddress;
    private String city;
    private String state;
    private String pincode;
    private String businessPhone;
    private String businessEmail;
    private String website;
    private Boolean gstinVerified;
    private Boolean panVerified;
    private Boolean isActive;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public BusinessResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }

    public IndustryCategory getIndustryCategory() { return industryCategory; }
    public void setIndustryCategory(IndustryCategory industryCategory) { this.industryCategory = industryCategory; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public String getBusinessDescription() { return businessDescription; }
    public void setBusinessDescription(String businessDescription) { this.businessDescription = businessDescription; }

    public String getRegisteredAddress() { return registeredAddress; }
    public void setRegisteredAddress(String registeredAddress) { this.registeredAddress = registeredAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getBusinessPhone() { return businessPhone; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }

    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Boolean getGstinVerified() { return gstinVerified; }
    public void setGstinVerified(Boolean gstinVerified) { this.gstinVerified = gstinVerified; }

    public Boolean getPanVerified() { return panVerified; }
    public void setPanVerified(Boolean panVerified) { this.panVerified = panVerified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}