package com.tcrs.tcrs_backend.repository;


import com.tcrs.tcrs_backend.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditMonitoringRepository extends JpaRepository<CreditMonitoring, Long> {

    // User's monitoring setups
    List<CreditMonitoring> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);

    Page<CreditMonitoring> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user, Pageable pageable);

    // Business monitoring
    List<CreditMonitoring> findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(Business business);

    Optional<CreditMonitoring> findByUserAndBusinessAndIsActiveTrue(User user, Business business);

    // Check for existing monitoring
    boolean existsByUserAndBusinessAndIsActiveTrue(User user, Business business);

    // Monitoring by type
    List<CreditMonitoring> findByMonitoringTypeAndIsActiveTrueOrderByCreatedAtDesc(MonitoringType monitoringType);

    // Monitoring due for checks
    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "(cm.lastCheckDate IS NULL OR cm.lastCheckDate < :cutoffTime)")
    List<CreditMonitoring> findMonitoringDueForCheck(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Active monitoring with specific notification preferences
    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "cm.emailNotifications = true AND cm.notificationFrequency = :frequency")
    List<CreditMonitoring> findActiveMonitoringByEmailNotificationFrequency(
            @Param("frequency") NotificationFrequency frequency);

    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "cm.inAppNotifications = true")
    List<CreditMonitoring> findActiveMonitoringWithInAppNotifications();

    // Statistics
    @Query("SELECT COUNT(cm) FROM CreditMonitoring cm WHERE cm.user = :user AND cm.isActive = true")
    Long countActiveMonitoringByUser(@Param("user") User user);

    @Query("SELECT COUNT(cm) FROM CreditMonitoring cm WHERE cm.business = :business AND cm.isActive = true")
    Long countActiveMonitoringByBusiness(@Param("business") Business business);

    // Find monitoring with specific alert types enabled
    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "cm.newTradeReferenceAlert = true")
    List<CreditMonitoring> findMonitoringWithTradeReferenceAlerts();

    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "cm.newPaymentHistoryAlert = true")
    List<CreditMonitoring> findMonitoringWithPaymentHistoryAlerts();

    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "cm.creditReportGenerationAlert = true")
    List<CreditMonitoring> findMonitoringWithCreditReportAlerts();

    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "cm.businessProfileChangeAlert = true")
    List<CreditMonitoring> findMonitoringWithBusinessProfileAlerts();

    // Find monitoring with credit score thresholds
    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "(cm.creditScoreThresholdMin IS NOT NULL OR cm.creditScoreThresholdMax IS NOT NULL)")
    List<CreditMonitoring> findMonitoringWithCreditScoreThresholds();

    // Recent activity
    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "cm.lastAlertDate >= :since ORDER BY cm.lastAlertDate DESC")
    List<CreditMonitoring> findMonitoringWithRecentAlerts(@Param("since") LocalDateTime since);

    // High-activity monitoring
    @Query("SELECT cm FROM CreditMonitoring cm WHERE cm.isActive = true AND " +
            "cm.totalAlertsSent >= :minAlerts ORDER BY cm.totalAlertsSent DESC")
    List<CreditMonitoring> findHighActivityMonitoring(@Param("minAlerts") Integer minAlerts);
}
