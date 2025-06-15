package com.tcrs.tcrs_backend.dto.reference;


import com.tcrs.tcrs_backend.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TradeReferenceResponse {

    private Long id;
    private Long businessId;
    private String businessName;
    private Long referenceProviderId;
    private String referenceProviderName;
    private String referenceNumber;
    private String companyName;
    private String contactPerson;
    private String designation;
    private String email;
    private String phone;
    private String companyAddress;
    private String companyGstin;
    private ReferenceType referenceType;
    private RelationshipType relationshipType;
    private Integer relationshipDurationMonths;
    private LocalDate relationshipStartDate;
    private LocalDate relationshipEndDate;
    private BigDecimal averageMonthlyBusiness;
    private BigDecimal totalBusinessValue;
    private BigDecimal creditLimitProvided;
    private String paymentTerms;
    private PaymentBehavior paymentBehavior;
    private Integer paymentRating;
    private Integer overallRating;
    private Boolean hasDisputes;
    private String disputeDetails;
    private String referenceComments;
    private RecommendationLevel recommendationLevel;
    private ReferenceVerificationStatus verificationStatus;
    private VerificationMethod verificationMethod;
    private String verifiedBy;
    private LocalDateTime verifiedDate;
    private String verificationNotes;
    private String referenceResponse;
    private LocalDateTime contactAttemptedDate;
    private Boolean isConfidential;
    private String addedByName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public TradeReferenceResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public Long getReferenceProviderId() { return referenceProviderId; }
    public void setReferenceProviderId(Long referenceProviderId) { this.referenceProviderId = referenceProviderId; }

    public String getReferenceProviderName() { return referenceProviderName; }
    public void setReferenceProviderName(String referenceProviderName) { this.referenceProviderName = referenceProviderName; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public String getCompanyGstin() { return companyGstin; }
    public void setCompanyGstin(String companyGstin) { this.companyGstin = companyGstin; }

    public ReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { this.referenceType = referenceType; }

    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }

    public Integer getRelationshipDurationMonths() { return relationshipDurationMonths; }
    public void setRelationshipDurationMonths(Integer relationshipDurationMonths) { this.relationshipDurationMonths = relationshipDurationMonths; }

    public LocalDate getRelationshipStartDate() { return relationshipStartDate; }
    public void setRelationshipStartDate(LocalDate relationshipStartDate) { this.relationshipStartDate = relationshipStartDate; }

    public LocalDate getRelationshipEndDate() { return relationshipEndDate; }
    public void setRelationshipEndDate(LocalDate relationshipEndDate) { this.relationshipEndDate = relationshipEndDate; }

    public BigDecimal getAverageMonthlyBusiness() { return averageMonthlyBusiness; }
    public void setAverageMonthlyBusiness(BigDecimal averageMonthlyBusiness) { this.averageMonthlyBusiness = averageMonthlyBusiness; }

    public BigDecimal getTotalBusinessValue() { return totalBusinessValue; }
    public void setTotalBusinessValue(BigDecimal totalBusinessValue) { this.totalBusinessValue = totalBusinessValue; }

    public BigDecimal getCreditLimitProvided() { return creditLimitProvided; }
    public void setCreditLimitProvided(BigDecimal creditLimitProvided) { this.creditLimitProvided = creditLimitProvided; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public PaymentBehavior getPaymentBehavior() { return paymentBehavior; }
    public void setPaymentBehavior(PaymentBehavior paymentBehavior) { this.paymentBehavior = paymentBehavior; }

    public Integer getPaymentRating() { return paymentRating; }
    public void setPaymentRating(Integer paymentRating) { this.paymentRating = paymentRating; }

    public Integer getOverallRating() { return overallRating; }
    public void setOverallRating(Integer overallRating) { this.overallRating = overallRating; }

    public Boolean getHasDisputes() { return hasDisputes; }
    public void setHasDisputes(Boolean hasDisputes) { this.hasDisputes = hasDisputes; }

    public String getDisputeDetails() { return disputeDetails; }
    public void setDisputeDetails(String disputeDetails) { this.disputeDetails = disputeDetails; }

    public String getReferenceComments() { return referenceComments; }
    public void setReferenceComments(String referenceComments) { this.referenceComments = referenceComments; }

    public RecommendationLevel getRecommendationLevel() { return recommendationLevel; }
    public void setRecommendationLevel(RecommendationLevel recommendationLevel) { this.recommendationLevel = recommendationLevel; }

    public ReferenceVerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(ReferenceVerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public VerificationMethod getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(VerificationMethod verificationMethod) { this.verificationMethod = verificationMethod; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getVerifiedDate() { return verifiedDate; }
    public void setVerifiedDate(LocalDateTime verifiedDate) { this.verifiedDate = verifiedDate; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public String getReferenceResponse() { return referenceResponse; }
    public void setReferenceResponse(String referenceResponse) { this.referenceResponse = referenceResponse; }

    public LocalDateTime getContactAttemptedDate() { return contactAttemptedDate; }
    public void setContactAttemptedDate(LocalDateTime contactAttemptedDate) { this.contactAttemptedDate = contactAttemptedDate; }

    public Boolean getIsConfidential() { return isConfidential; }
    public void setIsConfidential(Boolean isConfidential) { this.isConfidential = isConfidential; }

    public String getAddedByName() { return addedByName; }
    public void setAddedByName(String addedByName) { this.addedByName = addedByName; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
