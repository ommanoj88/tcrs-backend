package com.tcrs.tcrs_backend.dto.monitoring;


import com.tcrs.tcrs_backend.entity.AlertSeverity;
import com.tcrs.tcrs_backend.entity.AlertType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditAlertResponse {

    private Long id;
    private Long businessId;
    private String businessName;
    private String alertNumber;
    private AlertType alertType;
    private AlertSeverity severityLevel;
    private String title;
    private String description;
    private String details;

    // Comparison Values
    private String previousValue;
    private String currentValue;
    private String thresholdValue;
    private BigDecimal changeAmount;
    private BigDecimal changePercentage;

    // Status
    private Boolean isRead;
    private Boolean isAcknowledged;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedDate;
    private String acknowledgmentNotes;

    // Related Information
    private String relatedEntityType;
    private Long relatedEntityId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    // Constructors
    public CreditAlertResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getAlertNumber() { return alertNumber; }
    public void setAlertNumber(String alertNumber) { this.alertNumber = alertNumber; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public AlertSeverity getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(AlertSeverity severityLevel) { this.severityLevel = severityLevel; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getPreviousValue() { return previousValue; }
    public void setPreviousValue(String previousValue) { this.previousValue = previousValue; }

    public String getCurrentValue() { return currentValue; }
    public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }

    public String getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(String thresholdValue) { this.thresholdValue = thresholdValue; }

    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }

    public BigDecimal getChangePercentage() { return changePercentage; }
    public void setChangePercentage(BigDecimal changePercentage) { this.changePercentage = changePercentage; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Boolean getIsAcknowledged() { return isAcknowledged; }
    public void setIsAcknowledged(Boolean isAcknowledged) { this.isAcknowledged = isAcknowledged; }

    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public LocalDateTime getAcknowledgedDate() { return acknowledgedDate; }
    public void setAcknowledgedDate(LocalDateTime acknowledgedDate) { this.acknowledgedDate = acknowledgedDate; }

    public String getAcknowledgmentNotes() { return acknowledgmentNotes; }
    public void setAcknowledgmentNotes(String acknowledgmentNotes) { this.acknowledgmentNotes = acknowledgmentNotes; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
