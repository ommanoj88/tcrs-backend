package com.tcrs.tcrs_backend.entity;


import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_reports")
@EntityListeners(AuditingEntityListener.class)
public class CreditReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(name = "report_number", unique = true, nullable = false)
    private String reportNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_score_grade", nullable = false)
    private CreditGrade creditScoreGrade;

    @Column(name = "credit_score", precision = 5, scale = 2)
    private BigDecimal creditScore;

    @Column(name = "credit_limit_recommendation", precision = 15, scale = 2)
    private BigDecimal creditLimitRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category", nullable = false)
    private RiskCategory riskCategory;

    @Column(name = "financial_strength_score", precision = 5, scale = 2)
    private BigDecimal financialStrengthScore;

    @Column(name = "payment_behavior_score", precision = 5, scale = 2)
    private BigDecimal paymentBehaviorScore;

    @Column(name = "business_stability_score", precision = 5, scale = 2)
    private BigDecimal businessStabilityScore;

    @Column(name = "compliance_score", precision = 5, scale = 2)
    private BigDecimal complianceScore;

    @Column(name = "years_in_business")
    private Integer yearsInBusiness;

    @Column(name = "gst_compliance_status")
    private Boolean gstComplianceStatus;

    @Column(name = "pan_verification_status")
    private Boolean panVerificationStatus;

    @Column(name = "trade_references_count")
    private Integer tradeReferencesCount;

    @Column(name = "positive_references_count")
    private Integer positiveReferencesCount;

    @Column(name = "negative_references_count")
    private Integer negativeReferencesCount;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    @Column(name = "positive_indicators", columnDefinition = "TEXT")
    private String positiveIndicators;

    @Column(name = "report_valid_until")
    private LocalDateTime reportValidUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CreditReport() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }

    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}