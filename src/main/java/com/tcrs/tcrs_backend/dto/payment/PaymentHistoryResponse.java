package com.tcrs.tcrs_backend.dto.payment;

import com.tcrs.tcrs_backend.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentHistoryResponse {

    private Long id;
    private Long businessId;
    private String businessName;
    private String businessGstin;
    private String transactionReference;
    private String invoiceNumber;
    private BigDecimal transactionAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private PaymentStatus paymentStatus;
    private TransactionType transactionType;
    private Integer daysOverdue;
    private BigDecimal penaltyAmount;
    private BigDecimal settledAmount;
    private String paymentMethod;
    private String paymentTerms;
    private String tradeRelationship;
    private Integer paymentRating;
    private String comments;
    private VerificationStatus verificationStatus;
    private String verifiedBy;
    private LocalDateTime verifiedDate;
    private DisputeStatus disputeStatus;
    private String disputeReason;
    private String reportedByName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PaymentHistoryResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessGstin() { return businessGstin; }
    public void setBusinessGstin(String businessGstin) { this.businessGstin = businessGstin; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public Integer getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(Integer daysOverdue) { this.daysOverdue = daysOverdue; }

    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }

    public BigDecimal getSettledAmount() { return settledAmount; }
    public void setSettledAmount(BigDecimal settledAmount) { this.settledAmount = settledAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getTradeRelationship() { return tradeRelationship; }
    public void setTradeRelationship(String tradeRelationship) { this.tradeRelationship = tradeRelationship; }

    public Integer getPaymentRating() { return paymentRating; }
    public void setPaymentRating(Integer paymentRating) { this.paymentRating = paymentRating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getVerifiedDate() { return verifiedDate; }
    public void setVerifiedDate(LocalDateTime verifiedDate) { this.verifiedDate = verifiedDate; }

    public DisputeStatus getDisputeStatus() { return disputeStatus; }
    public void setDisputeStatus(DisputeStatus disputeStatus) { this.disputeStatus = disputeStatus; }

    public String getDisputeReason() { return disputeReason; }
    public void setDisputeReason(String disputeReason) { this.disputeReason = disputeReason; }

    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}