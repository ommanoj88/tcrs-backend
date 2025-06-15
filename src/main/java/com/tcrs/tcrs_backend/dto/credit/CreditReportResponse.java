package com.tcrs.tcrs_backend.dto.credit;

import com.tcrs.tcrs_backend.entity.CreditGrade;
import com.tcrs.tcrs_backend.entity.ReportStatus;
import com.tcrs.tcrs_backend.entity.RiskCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditReportResponse {

    private Long id;
    private String reportNumber;
    private Long businessId;
    private String businessName;
    private String businessGstin;
    private String businessPan;
    private CreditGrade creditScoreGrade;
    private BigDecimal creditScore;
    private BigDecimal creditLimitRecommendation;
    private RiskCategory riskCategory;

    // Component Scores
    private BigDecimal financialStrengthScore;
    private BigDecimal paymentBehaviorScore;
    private BigDecimal businessStabilityScore;
    private BigDecimal complianceScore;

    // Business Metrics
    private Integer yearsInBusiness;
    private Boolean gstComplianceStatus;
    private Boolean panVerificationStatus;
    private Integer tradeReferencesCount;
    private Integer positiveReferencesCount;
    private Integer negativeReferencesCount;

    // Report Content
    private String summary;
    private String recommendations;
    private String riskFactors;
    private String positiveIndicators;

    // Report Metadata
    private LocalDateTime reportValidUntil;
    private ReportStatus reportStatus;
    private String requestedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CreditReportResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessGstin() { return businessGstin; }
    public void setBusinessGstin(String businessGstin) { this.businessGstin = businessGstin; }

    public String getBusinessPan() { return businessPan; }
    public void setBusinessPan(String businessPan) { this.businessPan = businessPan; }

    public CreditGrade getCreditScoreGrade() { return creditScoreGrade; }
    public void setCreditScoreGrade(CreditGrade creditScoreGrade) { this.creditScoreGrade = creditScoreGrade; }

    public BigDecimal getCreditScore() { return creditScore; }
    public void setCreditScore(BigDecimal creditScore) { this.creditScore = creditScore; }

    public BigDecimal getCreditLimitRecommendation() { return creditLimitRecommendation; }
    public void setCreditLimitRecommendation(BigDecimal creditLimitRecommendation) {
        this.creditLimitRecommendation = creditLimitRecommendation;
    }

    public RiskCategory getRiskCategory() { return riskCategory; }
    public void setRiskCategory(RiskCategory riskCategory) { this.riskCategory = riskCategory; }

    public BigDecimal getFinancialStrengthScore() { return financialStrengthScore; }
    public void setFinancialStrengthScore(BigDecimal financialStrengthScore) {
        this.financialStrengthScore = financialStrengthScore;
    }

    public BigDecimal getPaymentBehaviorScore() { return paymentBehaviorScore; }
    public void setPaymentBehaviorScore(BigDecimal paymentBehaviorScore) {
        this.paymentBehaviorScore = paymentBehaviorScore;
    }

    public BigDecimal getBusinessStabilityScore() { return businessStabilityScore; }
    public void setBusinessStabilityScore(BigDecimal businessStabilityScore) {
        this.businessStabilityScore = businessStabilityScore;
    }

    public BigDecimal getComplianceScore() { return complianceScore; }
    public void setComplianceScore(BigDecimal complianceScore) { this.complianceScore = complianceScore; }

    public Integer getYearsInBusiness() { return yearsInBusiness; }
    public void setYearsInBusiness(Integer yearsInBusiness) { this.yearsInBusiness = yearsInBusiness; }

    public Boolean getGstComplianceStatus() { return gstComplianceStatus; }
    public void setGstComplianceStatus(Boolean gstComplianceStatus) { this.gstComplianceStatus = gstComplianceStatus; }

    public Boolean getPanVerificationStatus() { return panVerificationStatus; }
    public void setPanVerificationStatus(Boolean panVerificationStatus) {
        this.panVerificationStatus = panVerificationStatus;
    }

    public Integer getTradeReferencesCount() { return tradeReferencesCount; }
    public void setTradeReferencesCount(Integer tradeReferencesCount) {
        this.tradeReferencesCount = tradeReferencesCount;
    }

    public Integer getPositiveReferencesCount() { return positiveReferencesCount; }
    public void setPositiveReferencesCount(Integer positiveReferencesCount) {
        this.positiveReferencesCount = positiveReferencesCount;
    }

    public Integer getNegativeReferencesCount() { return negativeReferencesCount; }
    public void setNegativeReferencesCount(Integer negativeReferencesCount) {
        this.negativeReferencesCount = negativeReferencesCount;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public String getRiskFactors() { return riskFactors; }
    public void setRiskFactors(String riskFactors) { this.riskFactors = riskFactors; }

    public String getPositiveIndicators() { return positiveIndicators; }
    public void setPositiveIndicators(String positiveIndicators) { this.positiveIndicators = positiveIndicators; }

    public LocalDateTime getReportValidUntil() { return reportValidUntil; }
    public void setReportValidUntil(LocalDateTime reportValidUntil) { this.reportValidUntil = reportValidUntil; }

    public ReportStatus getReportStatus() { return reportStatus; }
    public void setReportStatus(ReportStatus reportStatus) { this.reportStatus = reportStatus; }

    public String getRequestedByName() { return requestedByName; }
    public void setRequestedByName(String requestedByName) { this.requestedByName = requestedByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
