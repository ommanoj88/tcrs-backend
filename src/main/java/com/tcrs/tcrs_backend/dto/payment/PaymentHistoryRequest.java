package com.tcrs.tcrs_backend.dto.payment;

import com.tcrs.tcrs_backend.entity.DisputeStatus;
import com.tcrs.tcrs_backend.entity.PaymentStatus;
import com.tcrs.tcrs_backend.entity.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentHistoryRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    private String invoiceNumber;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be greater than 0")
    private BigDecimal transactionAmount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private LocalDate paymentDate;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    private Integer daysOverdue;

    @DecimalMin(value = "0", message = "Penalty amount cannot be negative")
    private BigDecimal penaltyAmount;

    @DecimalMin(value = "0", message = "Settled amount cannot be negative")
    private BigDecimal settledAmount;

    private String paymentMethod;

    private String paymentTerms;

    @NotBlank(message = "Trade relationship is required")
    private String tradeRelationship;

    @Min(value = 1, message = "Payment rating must be between 1 and 5")
    @Max(value = 5, message = "Payment rating must be between 1 and 5")
    private Integer paymentRating;

    private String comments;

    private DisputeStatus disputeStatus;

    private String disputeReason;

    // Constructors
    public PaymentHistoryRequest() {}

    // Getters and Setters
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

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

    public DisputeStatus getDisputeStatus() { return disputeStatus; }
    public void setDisputeStatus(DisputeStatus disputeStatus) { this.disputeStatus = disputeStatus; }

    public String getDisputeReason() { return disputeReason; }
    public void setDisputeReason(String disputeReason) { this.disputeReason = disputeReason; }
}