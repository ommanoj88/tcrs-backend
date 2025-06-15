package com.tcrs.tcrs_backend.dto.payment;


import java.math.BigDecimal;
import java.util.Map;

public class PaymentAnalyticsResponse {

    private Long businessId;
    private String businessName;

    // Overall Statistics
    private Integer totalTransactions;
    private BigDecimal totalTransactionValue;
    private Double averagePaymentDelay;
    private Double onTimePaymentPercentage;
    private Double overduePaymentPercentage;
    private Integer totalOverdueTransactions;
    private BigDecimal totalOverdueAmount;

    // Payment Behavior Scores
    private Double paymentReliabilityScore;
    private Double paymentSpeedScore;
    private Double disputeFrequencyScore;
    private Double overallPaymentScore;

    // Payment Pattern Analysis
    private Map<String, Integer> paymentStatusDistribution;
    private Map<String, BigDecimal> paymentAmountByStatus;
    private Map<Integer, Integer> paymentDelayDistribution; // Days delay -> Count

    // Recent Trends (Last 6 months)
    private Map<String, Double> monthlyPaymentTrends;
    private Map<String, Integer> monthlyTransactionCounts;

    // Risk Indicators
    private Integer consecutiveDelays;
    private Integer totalDisputes;
    private BigDecimal largestOverdueAmount;
    private Integer longestPaymentDelay;

    // Trade Relationship Summary
    private Map<String, Integer> relationshipTypeBreakdown;
    private Map<String, Double> averageRatingByRelationship;

    // Constructors
    public PaymentAnalyticsResponse() {}

    // Getters and Setters
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

    public BigDecimal getTotalTransactionValue() { return totalTransactionValue; }
    public void setTotalTransactionValue(BigDecimal totalTransactionValue) { this.totalTransactionValue = totalTransactionValue; }

    public Double getAveragePaymentDelay() { return averagePaymentDelay; }
    public void setAveragePaymentDelay(Double averagePaymentDelay) { this.averagePaymentDelay = averagePaymentDelay; }

    public Double getOnTimePaymentPercentage() { return onTimePaymentPercentage; }
    public void setOnTimePaymentPercentage(Double onTimePaymentPercentage) { this.onTimePaymentPercentage = onTimePaymentPercentage; }

    public Double getOverduePaymentPercentage() { return overduePaymentPercentage; }
    public void setOverduePaymentPercentage(Double overduePaymentPercentage) { this.overduePaymentPercentage = overduePaymentPercentage; }

    public Integer getTotalOverdueTransactions() { return totalOverdueTransactions; }
    public void setTotalOverdueTransactions(Integer totalOverdueTransactions) { this.totalOverdueTransactions = totalOverdueTransactions; }

    public BigDecimal getTotalOverdueAmount() { return totalOverdueAmount; }
    public void setTotalOverdueAmount(BigDecimal totalOverdueAmount) { this.totalOverdueAmount = totalOverdueAmount; }

    public Double getPaymentReliabilityScore() { return paymentReliabilityScore; }
    public void setPaymentReliabilityScore(Double paymentReliabilityScore) { this.paymentReliabilityScore = paymentReliabilityScore; }

    public Double getPaymentSpeedScore() { return paymentSpeedScore; }
    public void setPaymentSpeedScore(Double paymentSpeedScore) { this.paymentSpeedScore = paymentSpeedScore; }

    public Double getDisputeFrequencyScore() { return disputeFrequencyScore; }
    public void setDisputeFrequencyScore(Double disputeFrequencyScore) { this.disputeFrequencyScore = disputeFrequencyScore; }

    public Double getOverallPaymentScore() { return overallPaymentScore; }
    public void setOverallPaymentScore(Double overallPaymentScore) { this.overallPaymentScore = overallPaymentScore; }

    public Map<String, Integer> getPaymentStatusDistribution() { return paymentStatusDistribution; }
    public void setPaymentStatusDistribution(Map<String, Integer> paymentStatusDistribution) { this.paymentStatusDistribution = paymentStatusDistribution; }

    public Map<String, BigDecimal> getPaymentAmountByStatus() { return paymentAmountByStatus; }
    public void setPaymentAmountByStatus(Map<String, BigDecimal> paymentAmountByStatus) { this.paymentAmountByStatus = paymentAmountByStatus; }

    public Map<Integer, Integer> getPaymentDelayDistribution() { return paymentDelayDistribution; }
    public void setPaymentDelayDistribution(Map<Integer, Integer> paymentDelayDistribution) { this.paymentDelayDistribution = paymentDelayDistribution; }

    public Map<String, Double> getMonthlyPaymentTrends() { return monthlyPaymentTrends; }
    public void setMonthlyPaymentTrends(Map<String, Double> monthlyPaymentTrends) { this.monthlyPaymentTrends = monthlyPaymentTrends; }

    public Map<String, Integer> getMonthlyTransactionCounts() { return monthlyTransactionCounts; }
    public void setMonthlyTransactionCounts(Map<String, Integer> monthlyTransactionCounts) { this.monthlyTransactionCounts = monthlyTransactionCounts; }

    public Integer getConsecutiveDelays() { return consecutiveDelays; }
    public void setConsecutiveDelays(Integer consecutiveDelays) { this.consecutiveDelays = consecutiveDelays; }

    public Integer getTotalDisputes() { return totalDisputes; }
    public void setTotalDisputes(Integer totalDisputes) { this.totalDisputes = totalDisputes; }

    public BigDecimal getLargestOverdueAmount() { return largestOverdueAmount; }
    public void setLargestOverdueAmount(BigDecimal largestOverdueAmount) { this.largestOverdueAmount = largestOverdueAmount; }

    public Integer getLongestPaymentDelay() { return longestPaymentDelay; }
    public void setLongestPaymentDelay(Integer longestPaymentDelay) { this.longestPaymentDelay = longestPaymentDelay; }

    public Map<String, Integer> getRelationshipTypeBreakdown() { return relationshipTypeBreakdown; }
    public void setRelationshipTypeBreakdown(Map<String, Integer> relationshipTypeBreakdown) { this.relationshipTypeBreakdown = relationshipTypeBreakdown; }

    public Map<String, Double> getAverageRatingByRelationship() { return averageRatingByRelationship; }
    public void setAverageRatingByRelationship(Map<String, Double> averageRatingByRelationship) { this.averageRatingByRelationship = averageRatingByRelationship; }
}