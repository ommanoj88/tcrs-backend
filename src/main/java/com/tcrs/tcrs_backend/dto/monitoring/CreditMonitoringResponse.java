package com.tcrs.tcrs_backend.dto.monitoring;


import com.tcrs.tcrs_backend.entity.MonitoringType;
import com.tcrs.tcrs_backend.entity.NotificationFrequency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditMonitoringResponse {

    private Long id;
    private Long businessId;
    private String businessName;
    private String monitoringName;
    private MonitoringType monitoringType;
    private Boolean isActive;

    // Thresholds
    private BigDecimal creditScoreThresholdMin;
    private BigDecimal creditScoreThresholdMax;
    private BigDecimal creditScoreChangeThreshold;
    private Integer paymentDelayThresholdDays;
    private BigDecimal overdueAmountThreshold;

    // Alert Types
    private Boolean newTradeReferenceAlert;
    private Boolean newPaymentHistoryAlert;
    private Boolean creditReportGenerationAlert;
    private Boolean businessProfileChangeAlert;

    // Notification Preferences
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean inAppNotifications;
    private NotificationFrequency notificationFrequency;

    // Status Information
    private LocalDateTime lastCheckDate;
    private LocalDateTime lastAlertDate;
    private Integer totalAlertsSent;
    private BigDecimal lastCreditScore;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CreditMonitoringResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getMonitoringName() { return monitoringName; }
    public void setMonitoringName(String monitoringName) { this.monitoringName = monitoringName; }

    public MonitoringType getMonitoringType() { return monitoringType; }
    public void setMonitoringType(MonitoringType monitoringType) { this.monitoringType = monitoringType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public BigDecimal getCreditScoreThresholdMin() { return creditScoreThresholdMin; }
    public void setCreditScoreThresholdMin(BigDecimal creditScoreThresholdMin) { this.creditScoreThresholdMin = creditScoreThresholdMin; }

    public BigDecimal getCreditScoreThresholdMax() { return creditScoreThresholdMax; }
    public void setCreditScoreThresholdMax(BigDecimal creditScoreThresholdMax) { this.creditScoreThresholdMax = creditScoreThresholdMax; }

    public BigDecimal getCreditScoreChangeThreshold() { return creditScoreChangeThreshold; }
    public void setCreditScoreChangeThreshold(BigDecimal creditScoreChangeThreshold) { this.creditScoreChangeThreshold = creditScoreChangeThreshold; }

    public Integer getPaymentDelayThresholdDays() { return paymentDelayThresholdDays; }
    public void setPaymentDelayThresholdDays(Integer paymentDelayThresholdDays) { this.paymentDelayThresholdDays = paymentDelayThresholdDays; }

    public BigDecimal getOverdueAmountThreshold() { return overdueAmountThreshold; }
    public void setOverdueAmountThreshold(BigDecimal overdueAmountThreshold) { this.overdueAmountThreshold = overdueAmountThreshold; }

    public Boolean getNewTradeReferenceAlert() { return newTradeReferenceAlert; }
    public void setNewTradeReferenceAlert(Boolean newTradeReferenceAlert) { this.newTradeReferenceAlert = newTradeReferenceAlert; }

    public Boolean getNewPaymentHistoryAlert() { return newPaymentHistoryAlert; }
    public void setNewPaymentHistoryAlert(Boolean newPaymentHistoryAlert) { this.newPaymentHistoryAlert = newPaymentHistoryAlert; }

    public Boolean getCreditReportGenerationAlert() { return creditReportGenerationAlert; }
    public void setCreditReportGenerationAlert(Boolean creditReportGenerationAlert) { this.creditReportGenerationAlert = creditReportGenerationAlert; }

    public Boolean getBusinessProfileChangeAlert() { return businessProfileChangeAlert; }
    public void setBusinessProfileChangeAlert(Boolean businessProfileChangeAlert) { this.businessProfileChangeAlert = businessProfileChangeAlert; }

    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public Boolean getSmsNotifications() { return smsNotifications; }
    public void setSmsNotifications(Boolean smsNotifications) { this.smsNotifications = smsNotifications; }

    public Boolean getInAppNotifications() { return inAppNotifications; }
    public void setInAppNotifications(Boolean inAppNotifications) { this.inAppNotifications = inAppNotifications; }

    public NotificationFrequency getNotificationFrequency() { return notificationFrequency; }
    public void setNotificationFrequency(NotificationFrequency notificationFrequency) { this.notificationFrequency = notificationFrequency; }

    public LocalDateTime getLastCheckDate() { return lastCheckDate; }
    public void setLastCheckDate(LocalDateTime lastCheckDate) { this.lastCheckDate = lastCheckDate; }

    public LocalDateTime getLastAlertDate() { return lastAlertDate; }
    public void setLastAlertDate(LocalDateTime lastAlertDate) { this.lastAlertDate = lastAlertDate; }

    public Integer getTotalAlertsSent() { return totalAlertsSent; }
    public void setTotalAlertsSent(Integer totalAlertsSent) { this.totalAlertsSent = totalAlertsSent; }

    public BigDecimal getLastCreditScore() { return lastCreditScore; }
    public void setLastCreditScore(BigDecimal lastCreditScore) { this.lastCreditScore = lastCreditScore; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}