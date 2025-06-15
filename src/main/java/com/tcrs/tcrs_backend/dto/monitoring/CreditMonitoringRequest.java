package com.tcrs.tcrs_backend.dto.monitoring;

import com.tcrs.tcrs_backend.entity.MonitoringType;
import com.tcrs.tcrs_backend.entity.NotificationFrequency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreditMonitoringRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotBlank(message = "Monitoring name is required")
    @Size(max = 100, message = "Monitoring name cannot exceed 100 characters")
    private String monitoringName;

    @NotNull(message = "Monitoring type is required")
    private MonitoringType monitoringType;

    // Credit Score Thresholds
    @DecimalMin(value = "0", message = "Credit score threshold minimum must be non-negative")
    @DecimalMax(value = "1000", message = "Credit score threshold minimum cannot exceed 1000")
    private BigDecimal creditScoreThresholdMin;

    @DecimalMin(value = "0", message = "Credit score threshold maximum must be non-negative")
    @DecimalMax(value = "1000", message = "Credit score threshold maximum cannot exceed 1000")
    private BigDecimal creditScoreThresholdMax;

    @DecimalMin(value = "1", message = "Credit score change threshold must be at least 1")
    @DecimalMax(value = "500", message = "Credit score change threshold cannot exceed 500")
    private BigDecimal creditScoreChangeThreshold;

    // Payment Thresholds
    @Min(value = 1, message = "Payment delay threshold must be at least 1 day")
    @Max(value = 365, message = "Payment delay threshold cannot exceed 365 days")
    private Integer paymentDelayThresholdDays;

    @DecimalMin(value = "0", message = "Overdue amount threshold must be non-negative")
    private BigDecimal overdueAmountThreshold;

    // Alert Types
    private Boolean newTradeReferenceAlert = true;
    private Boolean newPaymentHistoryAlert = true;
    private Boolean creditReportGenerationAlert = true;
    private Boolean businessProfileChangeAlert = true;

    // Notification Preferences
    private Boolean emailNotifications = true;
    private Boolean smsNotifications = false;
    private Boolean inAppNotifications = true;

    @NotNull(message = "Notification frequency is required")
    private NotificationFrequency notificationFrequency = NotificationFrequency.IMMEDIATE;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    // Constructors
    public CreditMonitoringRequest() {}

    // Getters and Setters
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getMonitoringName() { return monitoringName; }
    public void setMonitoringName(String monitoringName) { this.monitoringName = monitoringName; }

    public MonitoringType getMonitoringType() { return monitoringType; }
    public void setMonitoringType(MonitoringType monitoringType) { this.monitoringType = monitoringType; }

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

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
