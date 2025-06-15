package com.tcrs.tcrs_backend.dto.reference;

import com.tcrs.tcrs_backend.entity.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeReferenceRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    private Long referenceProviderId; // Optional - if reference provider is in our system

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    private String designation;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String companyAddress;

    private String companyGstin;

    @NotNull(message = "Reference type is required")
    private ReferenceType referenceType;

    @NotNull(message = "Relationship type is required")
    private RelationshipType relationshipType;

    private Integer relationshipDurationMonths;

    private LocalDate relationshipStartDate;

    private LocalDate relationshipEndDate;

    @DecimalMin(value = "0", message = "Average monthly business cannot be negative")
    private BigDecimal averageMonthlyBusiness;

    @DecimalMin(value = "0", message = "Total business value cannot be negative")
    private BigDecimal totalBusinessValue;

    @DecimalMin(value = "0", message = "Credit limit cannot be negative")
    private BigDecimal creditLimitProvided;

    private String paymentTerms;

    @NotNull(message = "Payment behavior is required")
    private PaymentBehavior paymentBehavior;

    @Min(value = 1, message = "Payment rating must be between 1 and 5")
    @Max(value = 5, message = "Payment rating must be between 1 and 5")
    private Integer paymentRating;

    @Min(value = 1, message = "Overall rating must be between 1 and 5")
    @Max(value = 5, message = "Overall rating must be between 1 and 5")
    private Integer overallRating;

    private Boolean hasDisputes;

    private String disputeDetails;

    private String referenceComments;

    private RecommendationLevel recommendationLevel;

    private Boolean isConfidential;

    // Constructors
    public TradeReferenceRequest() {}

    // Getters and Setters
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public Long getReferenceProviderId() { return referenceProviderId; }
    public void setReferenceProviderId(Long referenceProviderId) { this.referenceProviderId = referenceProviderId; }

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

    public Boolean getIsConfidential() { return isConfidential; }
    public void setIsConfidential(Boolean isConfidential) { this.isConfidential = isConfidential; }
}
