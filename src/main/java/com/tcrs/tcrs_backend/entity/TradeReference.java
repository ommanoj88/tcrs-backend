package com.tcrs.tcrs_backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_references")
@EntityListeners(AuditingEntityListener.class)
public class TradeReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business; // Business being referenced

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_provider_id")
    private Business referenceProvider; // Business providing the reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", nullable = false)
    private User addedBy; // User who added this reference

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contact_person", nullable = false)
    private String contactPerson;

    @Column(name = "designation")
    private String designation;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "company_address", columnDefinition = "TEXT")
    private String companyAddress;

    @Column(name = "company_gstin")
    private String companyGstin;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private ReferenceType referenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private RelationshipType relationshipType;

    @Column(name = "relationship_duration_months")
    private Integer relationshipDurationMonths;

    @Column(name = "relationship_start_date")
    private LocalDate relationshipStartDate;

    @Column(name = "relationship_end_date")
    private LocalDate relationshipEndDate;

    @Column(name = "average_monthly_business", precision = 15, scale = 2)
    private BigDecimal averageMonthlyBusiness;

    @Column(name = "total_business_value", precision = 15, scale = 2)
    private BigDecimal totalBusinessValue;

    @Column(name = "credit_limit_provided", precision = 15, scale = 2)
    private BigDecimal creditLimitProvided;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_behavior", nullable = false)
    private PaymentBehavior paymentBehavior;

    @Column(name = "payment_rating")
    private Integer paymentRating; // 1-5 scale

    @Column(name = "overall_rating")
    private Integer overallRating; // 1-5 scale

    @Column(name = "has_disputes")
    private Boolean hasDisputes = false;

    @Column(name = "dispute_details", columnDefinition = "TEXT")
    private String disputeDetails;

    @Column(name = "reference_comments", columnDefinition = "TEXT")
    private String referenceComments;

    @Column(name = "recommendation_level")
    @Enumerated(EnumType.STRING)
    private RecommendationLevel recommendationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private ReferenceVerificationStatus verificationStatus;

    @Column(name = "verification_method")
    @Enumerated(EnumType.STRING)
    private VerificationMethod verificationMethod;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "reference_response", columnDefinition = "TEXT")
    private String referenceResponse;

    @Column(name = "contact_attempted_date")
    private LocalDateTime contactAttemptedDate;

    @Column(name = "is_confidential")
    private Boolean isConfidential = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public TradeReference() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public Business getReferenceProvider() { return referenceProvider; }
    public void setReferenceProvider(Business referenceProvider) { this.referenceProvider = referenceProvider; }

    public User getAddedBy() { return addedBy; }
    public void setAddedBy(User addedBy) { this.addedBy = addedBy; }

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

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
