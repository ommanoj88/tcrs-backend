package com.tcrs.tcrs_backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_alerts")
@EntityListeners(AuditingEntityListener.class)
public class CreditAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_monitoring_id", nullable = false)
    private CreditMonitoring creditMonitoring;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "alert_number", unique = true, nullable = false)
    private String alertNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false)
    private AlertSeverity severityLevel;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON or detailed information

    // Values for comparison
    @Column(name = "previous_value")
    private String previousValue;

    @Column(name = "current_value")
    private String currentValue;

    @Column(name = "threshold_value")
    private String thresholdValue;

    @Column(name = "change_amount", precision = 15, scale = 2)
    private BigDecimal changeAmount;

    @Column(name = "change_percentage", precision = 5, scale = 2)
    private BigDecimal changePercentage;

    // Notification Status
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_acknowledged", nullable = false)
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_by")
    private String acknowledgedBy;

    @Column(name = "acknowledged_date")
    private LocalDateTime acknowledgedDate;

    @Column(name = "acknowledgment_notes", columnDefinition = "TEXT")
    private String acknowledgmentNotes;

    // Notification Delivery Status
    @Column(name = "email_sent")
    private Boolean emailSent = false;

    @Column(name = "email_sent_date")
    private LocalDateTime emailSentDate;

    @Column(name = "sms_sent")
    private Boolean smsSent = false;

    @Column(name = "sms_sent_date")
    private LocalDateTime smsSentDate;

    @Column(name = "in_app_notified")
    private Boolean inAppNotified = false;

    @Column(name = "in_app_notified_date")
    private LocalDateTime inAppNotifiedDate;

    // Related Entity Information
    @Column(name = "related_entity_type")
    private String relatedEntityType; // CreditReport, PaymentHistory, TradeReference, etc.

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public CreditAlert() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CreditMonitoring getCreditMonitoring() { return creditMonitoring; }
    public void setCreditMonitoring(CreditMonitoring creditMonitoring) { this.creditMonitoring = creditMonitoring; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

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

    public Boolean getEmailSent() { return emailSent; }
    public void setEmailSent(Boolean emailSent) { this.emailSent = emailSent; }

    public LocalDateTime getEmailSentDate() { return emailSentDate; }
    public void setEmailSentDate(LocalDateTime emailSentDate) { this.emailSentDate = emailSentDate; }

    public Boolean getSmsSent() { return smsSent; }
    public void setSmsSent(Boolean smsSent) { this.smsSent = smsSent; }

    public LocalDateTime getSmsSentDate() { return smsSentDate; }
    public void setSmsSentDate(LocalDateTime smsSentDate) { this.smsSentDate = smsSentDate; }

    public Boolean getInAppNotified() { return inAppNotified; }
    public void setInAppNotified(Boolean inAppNotified) { this.inAppNotified = inAppNotified; }

    public LocalDateTime getInAppNotifiedDate() { return inAppNotifiedDate; }
    public void setInAppNotifiedDate(LocalDateTime inAppNotifiedDate) { this.inAppNotifiedDate = inAppNotifiedDate; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}