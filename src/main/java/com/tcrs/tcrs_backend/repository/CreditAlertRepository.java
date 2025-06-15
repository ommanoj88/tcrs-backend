package com.tcrs.tcrs_backend.repository;


import com.tcrs.tcrs_backend.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditAlertRepository extends JpaRepository<CreditAlert, Long> {

    // User alerts
    Page<CreditAlert> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user, Pageable pageable);

    List<CreditAlert> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);

    // Unread alerts
    Page<CreditAlert> findByUserAndIsReadFalseAndIsActiveTrueOrderByCreatedAtDesc(User user, Pageable pageable);

    List<CreditAlert> findByUserAndIsReadFalseAndIsActiveTrueOrderByCreatedAtDesc(User user);

    // Unacknowledged alerts
    Page<CreditAlert> findByUserAndIsAcknowledgedFalseAndIsActiveTrueOrderByCreatedAtDesc(User user, Pageable pageable);

    // Business alerts
    List<CreditAlert> findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(Business business);

    Page<CreditAlert> findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(Business business, Pageable pageable);

    // Alert by number
    Optional<CreditAlert> findByAlertNumberAndIsActiveTrue(String alertNumber);

    // Alerts by type and severity
    List<CreditAlert> findByAlertTypeAndIsActiveTrueOrderByCreatedAtDesc(AlertType alertType);

    List<CreditAlert> findBySeverityLevelAndIsActiveTrueOrderByCreatedAtDesc(AlertSeverity severityLevel);

    Page<CreditAlert> findByUserAndAlertTypeAndIsActiveTrueOrderByCreatedAtDesc(
            User user, AlertType alertType, Pageable pageable);

    Page<CreditAlert> findByUserAndSeverityLevelAndIsActiveTrueOrderByCreatedAtDesc(
            User user, AlertSeverity severityLevel, Pageable pageable);

    // Date range queries
    @Query("SELECT ca FROM CreditAlert ca WHERE ca.user = :user AND ca.isActive = true AND " +
            "ca.createdAt BETWEEN :startDate AND :endDate ORDER BY ca.createdAt DESC")
    List<CreditAlert> findByUserAndDateRange(@Param("user") User user,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // Statistics
    @Query("SELECT COUNT(ca) FROM CreditAlert ca WHERE ca.user = :user AND ca.isActive = true")
    Long countTotalAlertsByUser(@Param("user") User user);

    @Query("SELECT COUNT(ca) FROM CreditAlert ca WHERE ca.user = :user AND ca.isRead = false AND ca.isActive = true")
    Long countUnreadAlertsByUser(@Param("user") User user);

    @Query("SELECT COUNT(ca) FROM CreditAlert ca WHERE ca.user = :user AND ca.isAcknowledged = false AND ca.isActive = true")
    Long countUnacknowledgedAlertsByUser(@Param("user") User user);

    @Query("SELECT COUNT(ca) FROM CreditAlert ca WHERE ca.business = :business AND ca.isActive = true")
    Long countAlertsByBusiness(@Param("business") Business business);

    @Query("SELECT COUNT(ca) FROM CreditAlert ca WHERE ca.user = :user AND ca.severityLevel = :severity AND ca.isActive = true")
    Long countAlertsByUserAndSeverity(@Param("user") User user, @Param("severity") AlertSeverity severity);

    // Recent alerts
    @Query("SELECT ca FROM CreditAlert ca WHERE ca.user = :user AND ca.isActive = true AND " +
            "ca.createdAt >= :since ORDER BY ca.createdAt DESC")
    List<CreditAlert> findRecentAlertsByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    // High priority unread alerts
    @Query("SELECT ca FROM CreditAlert ca WHERE ca.user = :user AND ca.isRead = false AND ca.isActive = true AND " +
            "ca.severityLevel IN ('HIGH', 'CRITICAL') ORDER BY ca.severityLevel DESC, ca.createdAt DESC")
    List<CreditAlert> findHighPriorityUnreadAlerts(@Param("user") User user);

    // Alerts by monitoring
    List<CreditAlert> findByCreditMonitoringAndIsActiveTrueOrderByCreatedAtDesc(CreditMonitoring creditMonitoring);

    @Query("SELECT ca FROM CreditAlert ca WHERE ca.creditMonitoring = :monitoring AND ca.isActive = true AND " +
            "ca.createdAt >= :since ORDER BY ca.createdAt DESC")
    List<CreditAlert> findRecentAlertsByMonitoring(@Param("monitoring") CreditMonitoring monitoring,
                                                   @Param("since") LocalDateTime since);

    // Notification status queries
    @Query("SELECT ca FROM CreditAlert ca WHERE ca.isActive = true AND " +
            "(ca.emailSent = false AND ca.user IN " +
            "(SELECT cm.user FROM CreditMonitoring cm WHERE cm.emailNotifications = true AND cm.isActive = true))")
    List<CreditAlert> findAlertsNeedingEmailNotification();

    @Query("SELECT ca FROM CreditAlert ca WHERE ca.isActive = true AND " +
            "(ca.smsSent = false AND ca.user IN " +
            "(SELECT cm.user FROM CreditMonitoring cm WHERE cm.smsNotifications = true AND cm.isActive = true))")
    List<CreditAlert> findAlertsNeedingSmsNotification();

    @Query("SELECT ca FROM CreditAlert ca WHERE ca.isActive = true AND ca.inAppNotified = false")
    List<CreditAlert> findAlertsNeedingInAppNotification();

    // Expired alerts
    @Query("SELECT ca FROM CreditAlert ca WHERE ca.isActive = true AND ca.expiresAt IS NOT NULL AND ca.expiresAt < :now")
    List<CreditAlert> findExpiredAlerts(@Param("now") LocalDateTime now);

    // Alert distribution by type for user
    @Query("SELECT ca.alertType, COUNT(ca) FROM CreditAlert ca WHERE ca.user = :user AND ca.isActive = true " +
            "AND ca.createdAt >= :since GROUP BY ca.alertType")
    List<Object[]> getAlertTypeDistributionByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    // Alert distribution by severity for user
    @Query("SELECT ca.severityLevel, COUNT(ca) FROM CreditAlert ca WHERE ca.user = :user AND ca.isActive = true " +
            "AND ca.createdAt >= :since GROUP BY ca.severityLevel")
    List<Object[]> getAlertSeverityDistributionByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    // Related entity alerts
    @Query("SELECT ca FROM CreditAlert ca WHERE ca.relatedEntityType = :entityType AND ca.relatedEntityId = :entityId " +
            "AND ca.isActive = true ORDER BY ca.createdAt DESC")
    List<CreditAlert> findByRelatedEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    // Bulk operations
    @Modifying
    @Query("UPDATE CreditAlert ca SET ca.isRead = true WHERE ca.user = :user AND ca.isRead = false")
    int markAllAlertsAsReadForUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE CreditAlert ca SET ca.isAcknowledged = true, ca.acknowledgedBy = :acknowledgedBy, " +
            "ca.acknowledgedDate = :acknowledgedDate WHERE ca.user = :user AND ca.isAcknowledged = false")
    int acknowledgeAllAlertsForUser(@Param("user") User user, @Param("acknowledgedBy") String acknowledgedBy,
                                    @Param("acknowledgedDate") LocalDateTime acknowledgedDate);

    @Modifying
    @Query("UPDATE CreditAlert ca SET ca.isActive = false WHERE ca.expiresAt < :now")
    int deactivateExpiredAlerts(@Param("now") LocalDateTime now);

    // Search functionality
    @Query("SELECT ca FROM CreditAlert ca WHERE ca.user = :user AND ca.isActive = true AND " +
            "(LOWER(ca.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(ca.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY ca.createdAt DESC")
    List<CreditAlert> searchAlerts(@Param("user") User user, @Param("searchTerm") String searchTerm);
}