package com.tcrs.tcrs_backend.entity;


import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_monitoring")
@EntityListeners(AuditingEntityListener.class)
public class CreditMonitoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User monitoring the business

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business; // Business being monitored

    @Column(name = "monitoring_name", nullable = false)
    private String monitoringName; // Custom name for this monitoring

    @Enumerated(EnumType.STRING)
    @Column(name = "monitoring_type", nullable = false)
    private MonitoringType monitoringType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Alert Thresholds
    @Column(name = "credit_score_threshold_min")
    private BigDecimal creditScoreThresholdMin;

    @Column(name = "credit_score_threshold_max")
    private BigDecimal creditScoreThresholdMax;

    @Column(name = "credit_score_change_threshold")
    private BigDecimal creditScoreChangeThreshold; // Alert on X point change

    @Column(name = "payment_delay_threshold_days")
    private Integer paymentDelayThresholdDays;

    @Column(name = "overdue_amount_threshold", precision = 15, scale = 2)
    private BigDecimal overdueAmountThreshold;

    @Column(name = "new_trade_reference_alert")
    private Boolean newTradeReferenceAlert = true;

    @Column(name = "new_payment_history_alert")
    private Boolean newPaymentHistoryAlert = true;

    @Column(name = "credit_report_generation_alert")
    private Boolean creditReportGenerationAlert = true;

    @Column(name = "business_profile_change_alert")
    private Boolean businessProfileChangeAlert = true;

    // Notification Preferences
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "sms_notifications")
    private Boolean smsNotifications = false;

    @Column(name = "in_app_notifications")
    private Boolean inAppNotifications = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_frequency")
    private NotificationFrequency notificationFrequency = NotificationFrequency.IMMEDIATE;

    // Monitoring Status
    @Column(name = "last_check_date")
    private LocalDateTime lastCheckDate;

    @Column(name = "last_alert_date")
    private LocalDateTime lastAlertDate;

    @Column(name = "total_alerts_sent")
    private Integer totalAlertsSent = 0;

    @Column(name = "last_credit_score")
    private BigDecimal lastCreditScore;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CreditMonitoring() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

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
