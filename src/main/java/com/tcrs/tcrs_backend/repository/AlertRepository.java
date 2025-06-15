package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.Alert;
import com.tcrs.tcrs_backend.entity.AlertSeverity;
import com.tcrs.tcrs_backend.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Count methods for analytics
    long countByIsReadFalse();
    long countByIsAcknowledgedTrue();
    long countBySeverity(AlertSeverity severity);
    long countByAlertType(AlertType alertType);

    // Find alerts by various criteria
    List<Alert> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Alert> findByIsReadFalseOrderByCreatedAtDesc();
    List<Alert> findBySeverityOrderByCreatedAtDesc(AlertSeverity severity);
    List<Alert> findByAlertTypeOrderByCreatedAtDesc(AlertType alertType);

    // Date range queries
    List<Alert> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Combined criteria
    List<Alert> findByBusinessIdAndIsReadFalse(Long businessId);
    List<Alert> findByUserIdAndIsReadFalse(Long userId);
    List<Alert> findBySeverityAndIsReadFalse(AlertSeverity severity);

    // Analytics queries
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.createdAt >= :fromDate")
    long countAlertsFromDate(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT a.severity, COUNT(a) FROM Alert a GROUP BY a.severity")
    List<Object[]> countAlertsBySeverity();

    @Query("SELECT a.alertType, COUNT(a) FROM Alert a GROUP BY a.alertType")
    List<Object[]> countAlertsByType();

    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM Alert a WHERE a.createdAt >= :fromDate GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt)")
    List<Object[]> getAlertCountsByDate(@Param("fromDate") LocalDateTime fromDate);

    // Business-specific analytics
    @Query("SELECT a.business.id, COUNT(a) FROM Alert a WHERE a.business IS NOT NULL GROUP BY a.business.id ORDER BY COUNT(a) DESC")
    List<Object[]> getAlertCountsByBusiness();

    // User-specific analytics
    @Query("SELECT a.user.id, COUNT(a) FROM Alert a WHERE a.user IS NOT NULL GROUP BY a.user.id ORDER BY COUNT(a) DESC")
    List<Object[]> getAlertCountsByUser();
}