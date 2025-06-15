package com.tcrs.tcrs_backend.dto.reference;

import java.math.BigDecimal;
import java.util.Map;

public class TradeReferenceAnalyticsResponse {

    private Long businessId;
    private String businessName;

    // Overall Statistics
    private Integer totalReferences;
    private Integer verifiedReferences;
    private Integer pendingReferences;
    private Integer positiveReferences;
    private Integer negativeReferences;
    private Integer confidentialReferences;
    private Integer referencesWithDisputes;

    // Ratings and Scores
    private Double averagePaymentRating;
    private Double averageOverallRating;
    private Double verificationRate;
    private Double positiveReferenceRate;
    private Double disputeRate;

    // Business Value Metrics
    private BigDecimal totalBusinessValue;
    private BigDecimal averageMonthlyBusinessTotal;
    private BigDecimal averageBusinessValuePerReference;
    private BigDecimal totalCreditLimitProvided;

    // Reference Distribution
    private Map<String, Integer> referenceTypeDistribution;
    private Map<String, Integer> relationshipTypeDistribution;
    private Map<String, Integer> paymentBehaviorDistribution;
    private Map<String, Integer> recommendationLevelDistribution;
    private Map<String, Integer> verificationStatusDistribution;

    // Relationship Analysis
    private Integer longTermRelationships; // > 24 months
    private Integer highValueRelationships; // > 1 Crore business value
    private Double averageRelationshipDuration;

    // Performance Indicators
    private Integer excellentPaymentBehaviorCount;
    private Integer highlyRecommendedCount;
    private Integer recentVerifications; // Last 30 days

    // Constructors
    public TradeReferenceAnalyticsResponse() {}

    // Getters and Setters
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public Integer getTotalReferences() { return totalReferences; }
    public void setTotalReferences(Integer totalReferences) { this.totalReferences = totalReferences; }

    public Integer getVerifiedReferences() { return verifiedReferences; }
    public void setVerifiedReferences(Integer verifiedReferences) { this.verifiedReferences = verifiedReferences; }

    public Integer getPendingReferences() { return pendingReferences; }
    public void setPendingReferences(Integer pendingReferences) { this.pendingReferences = pendingReferences; }

    public Integer getPositiveReferences() { return positiveReferences; }
    public void setPositiveReferences(Integer positiveReferences) { this.positiveReferences = positiveReferences; }

    public Integer getNegativeReferences() { return negativeReferences; }
    public void setNegativeReferences(Integer negativeReferences) { this.negativeReferences = negativeReferences; }

    public Integer getConfidentialReferences() { return confidentialReferences; }
    public void setConfidentialReferences(Integer confidentialReferences) { this.confidentialReferences = confidentialReferences; }

    public Integer getReferencesWithDisputes() { return referencesWithDisputes; }
    public void setReferencesWithDisputes(Integer referencesWithDisputes) { this.referencesWithDisputes = referencesWithDisputes; }

    public Double getAveragePaymentRating() { return averagePaymentRating; }
    public void setAveragePaymentRating(Double averagePaymentRating) { this.averagePaymentRating = averagePaymentRating; }

    public Double getAverageOverallRating() { return averageOverallRating; }
    public void setAverageOverallRating(Double averageOverallRating) { this.averageOverallRating = averageOverallRating; }

    public Double getVerificationRate() { return verificationRate; }
    public void setVerificationRate(Double verificationRate) { this.verificationRate = verificationRate; }

    public Double getPositiveReferenceRate() { return positiveReferenceRate; }
    public void setPositiveReferenceRate(Double positiveReferenceRate) { this.positiveReferenceRate = positiveReferenceRate; }

    public Double getDisputeRate() { return disputeRate; }
    public void setDisputeRate(Double disputeRate) { this.disputeRate = disputeRate; }

    public BigDecimal getTotalBusinessValue() { return totalBusinessValue; }
    public void setTotalBusinessValue(BigDecimal totalBusinessValue) { this.totalBusinessValue = totalBusinessValue; }

    public BigDecimal getAverageMonthlyBusinessTotal() { return averageMonthlyBusinessTotal; }
    public void setAverageMonthlyBusinessTotal(BigDecimal averageMonthlyBusinessTotal) { this.averageMonthlyBusinessTotal = averageMonthlyBusinessTotal; }

    public BigDecimal getAverageBusinessValuePerReference() { return averageBusinessValuePerReference; }
    public void setAverageBusinessValuePerReference(BigDecimal averageBusinessValuePerReference) { this.averageBusinessValuePerReference = averageBusinessValuePerReference; }

    public BigDecimal getTotalCreditLimitProvided() { return totalCreditLimitProvided; }
    public void setTotalCreditLimitProvided(BigDecimal totalCreditLimitProvided) { this.totalCreditLimitProvided = totalCreditLimitProvided; }

    public Map<String, Integer> getReferenceTypeDistribution() { return referenceTypeDistribution; }
    public void setReferenceTypeDistribution(Map<String, Integer> referenceTypeDistribution) { this.referenceTypeDistribution = referenceTypeDistribution; }

    public Map<String, Integer> getRelationshipTypeDistribution() { return relationshipTypeDistribution; }
    public void setRelationshipTypeDistribution(Map<String, Integer> relationshipTypeDistribution) { this.relationshipTypeDistribution = relationshipTypeDistribution; }

    public Map<String, Integer> getPaymentBehaviorDistribution() { return paymentBehaviorDistribution; }
    public void setPaymentBehaviorDistribution(Map<String, Integer> paymentBehaviorDistribution) { this.paymentBehaviorDistribution = paymentBehaviorDistribution; }

    public Map<String, Integer> getRecommendationLevelDistribution() { return recommendationLevelDistribution; }
    public void setRecommendationLevelDistribution(Map<String, Integer> recommendationLevelDistribution) { this.recommendationLevelDistribution = recommendationLevelDistribution; }

    public Map<String, Integer> getVerificationStatusDistribution() { return verificationStatusDistribution; }
    public void setVerificationStatusDistribution(Map<String, Integer> verificationStatusDistribution) { this.verificationStatusDistribution = verificationStatusDistribution; }

    public Integer getLongTermRelationships() { return longTermRelationships; }
    public void setLongTermRelationships(Integer longTermRelationships) { this.longTermRelationships = longTermRelationships; }

    public Integer getHighValueRelationships() { return highValueRelationships; }
    public void setHighValueRelationships(Integer highValueRelationships) { this.highValueRelationships = highValueRelationships; }

    public Double getAverageRelationshipDuration() { return averageRelationshipDuration; }
    public void setAverageRelationshipDuration(Double averageRelationshipDuration) { this.averageRelationshipDuration = averageRelationshipDuration; }

    public Integer getExcellentPaymentBehaviorCount() { return excellentPaymentBehaviorCount; }
    public void setExcellentPaymentBehaviorCount(Integer excellentPaymentBehaviorCount) { this.excellentPaymentBehaviorCount = excellentPaymentBehaviorCount; }

    public Integer getHighlyRecommendedCount() { return highlyRecommendedCount; }
    public void setHighlyRecommendedCount(Integer highlyRecommendedCount) { this.highlyRecommendedCount = highlyRecommendedCount; }

    public Integer getRecentVerifications() { return recentVerifications; }
    public void setRecentVerifications(Integer recentVerifications) { this.recentVerifications = recentVerifications; }
}
