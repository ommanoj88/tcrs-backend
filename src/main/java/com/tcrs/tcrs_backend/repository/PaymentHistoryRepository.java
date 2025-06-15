package com.tcrs.tcrs_backend.repository;


import com.tcrs.tcrs_backend.entity.Business;
import com.tcrs.tcrs_backend.entity.PaymentHistory;
import com.tcrs.tcrs_backend.entity.PaymentStatus;
import com.tcrs.tcrs_backend.entity.User;
import com.tcrs.tcrs_backend.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    // Basic Queries
    List<PaymentHistory> findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(Business business);

    Page<PaymentHistory> findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(Business business, Pageable pageable);

    List<PaymentHistory> findByReportedByAndIsActiveTrueOrderByCreatedAtDesc(User reportedBy);

    Page<PaymentHistory> findByReportedByAndIsActiveTrueOrderByCreatedAtDesc(User reportedBy, Pageable pageable);

    Optional<PaymentHistory> findByTransactionReferenceAndIsActiveTrue(String transactionReference);

    // Payment Status Queries
    List<PaymentHistory> findByBusinessAndPaymentStatusAndIsActiveTrueOrderByDueDateDesc(
            Business business, PaymentStatus paymentStatus);

    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.paymentStatus IN ('OVERDUE', 'DEFAULTED') ORDER BY ph.dueDate DESC")
    List<PaymentHistory> findOverduePaymentsByBusiness(@Param("business") Business business);

    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.paymentStatus = 'PAID' AND ph.daysOverdue IS NOT NULL AND ph.daysOverdue > 0 " +
            "ORDER BY ph.paymentDate DESC")
    List<PaymentHistory> findLatePaymentsByBusiness(@Param("business") Business business);

    // Analytics Queries
    @Query("SELECT COUNT(ph) FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true")
    Long countTotalTransactionsByBusiness(@Param("business") Business business);

    @Query("SELECT SUM(ph.transactionAmount) FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true")
    BigDecimal sumTotalTransactionValueByBusiness(@Param("business") Business business);

    @Query("SELECT COUNT(ph) FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.paymentStatus = 'PAID' AND (ph.daysOverdue IS NULL OR ph.daysOverdue = 0)")
    Long countOnTimePaymentsByBusiness(@Param("business") Business business);

    @Query("SELECT COUNT(ph) FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.paymentStatus IN ('OVERDUE', 'DEFAULTED')")
    Long countOverdueTransactionsByBusiness(@Param("business") Business business);

    @Query("SELECT SUM(ph.transactionAmount) FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.paymentStatus IN ('OVERDUE', 'DEFAULTED')")
    BigDecimal sumOverdueAmountByBusiness(@Param("business") Business business);

    @Query("SELECT AVG(CAST(ph.daysOverdue AS double)) FROM PaymentHistory ph WHERE ph.business = :business " +
            "AND ph.isActive = true AND ph.daysOverdue IS NOT NULL AND ph.daysOverdue > 0")
    Double averagePaymentDelayByBusiness(@Param("business") Business business);

    @Query("SELECT AVG(CAST(ph.paymentRating AS double)) FROM PaymentHistory ph WHERE ph.business = :business " +
            "AND ph.isActive = true AND ph.paymentRating IS NOT NULL")
    Double averagePaymentRatingByBusiness(@Param("business") Business business);

    // Recent Payment History (Last 6 months)
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.createdAt >= :sixMonthsAgo ORDER BY ph.createdAt DESC")
    List<PaymentHistory> findRecentPaymentHistory(@Param("business") Business business,
                                                  @Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);

    // Payment Status Distribution
    @Query("SELECT ph.paymentStatus, COUNT(ph) FROM PaymentHistory ph WHERE ph.business = :business " +
            "AND ph.isActive = true GROUP BY ph.paymentStatus")
    List<Object[]> getPaymentStatusDistribution(@Param("business") Business business);

    @Query("SELECT ph.paymentStatus, SUM(ph.transactionAmount) FROM PaymentHistory ph WHERE ph.business = :business " +
            "AND ph.isActive = true GROUP BY ph.paymentStatus")
    List<Object[]> getPaymentAmountByStatus(@Param("business") Business business);

    // Trade Relationship Analysis
    @Query("SELECT ph.tradeRelationship, COUNT(ph) FROM PaymentHistory ph WHERE ph.business = :business " +
            "AND ph.isActive = true GROUP BY ph.tradeRelationship")
    List<Object[]> getRelationshipTypeBreakdown(@Param("business") Business business);

    @Query("SELECT ph.tradeRelationship, AVG(CAST(ph.paymentRating AS double)) FROM PaymentHistory ph " +
            "WHERE ph.business = :business AND ph.isActive = true AND ph.paymentRating IS NOT NULL " +
            "GROUP BY ph.tradeRelationship")
    List<Object[]> getAverageRatingByRelationship(@Param("business") Business business);

    // Risk Indicators
    @Query("SELECT MAX(ph.transactionAmount) FROM PaymentHistory ph WHERE ph.business = :business " +
            "AND ph.isActive = true AND ph.paymentStatus IN ('OVERDUE', 'DEFAULTED')")
    BigDecimal findLargestOverdueAmount(@Param("business") Business business);

    @Query("SELECT MAX(ph.daysOverdue) FROM PaymentHistory ph WHERE ph.business = :business " +
            "AND ph.isActive = true AND ph.daysOverdue IS NOT NULL")
    Integer findLongestPaymentDelay(@Param("business") Business business);

    @Query("SELECT COUNT(ph) FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.disputeStatus IN ('DISPUTE_RAISED', 'UNDER_REVIEW', 'ESCALATED')")
    Long countActiveDisputes(@Param("business") Business business);

    // Date Range Queries
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.dueDate BETWEEN :startDate AND :endDate ORDER BY ph.dueDate DESC")
    List<PaymentHistory> findByBusinessAndDateRange(@Param("business") Business business,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // Verification Status Queries
    List<PaymentHistory> findByVerificationStatusAndIsActiveTrueOrderByCreatedAtDesc(VerificationStatus status);

    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.verificationStatus = :status ORDER BY ph.createdAt DESC")
    List<PaymentHistory> findByBusinessAndVerificationStatus(@Param("business") Business business,
                                                             @Param("status") VerificationStatus status);

    // Monthly Trend Analysis
    @Query("SELECT YEAR(ph.dueDate), MONTH(ph.dueDate), COUNT(ph), AVG(CAST(ph.daysOverdue AS double)) " +
            "FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.dueDate >= :startDate GROUP BY YEAR(ph.dueDate), MONTH(ph.dueDate) " +
            "ORDER BY YEAR(ph.dueDate), MONTH(ph.dueDate)")
    List<Object[]> getMonthlyPaymentTrends(@Param("business") Business business,
                                           @Param("startDate") LocalDate startDate);

    // Performance Metrics
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.business = :business AND ph.isActive = true " +
            "AND ph.paymentStatus = 'PAID' ORDER BY ph.paymentDate DESC")
    List<PaymentHistory> findConsecutivePayments(@Param("business") Business business, Pageable pageable);

    // Add these methods to your existing PaymentHistoryRepository.java:

    List<PaymentHistory> findByBusinessIdAndIsActiveTrue(Long businessId);

    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.business.id = :businessId AND ph.isActive = true ORDER BY ph.createdAt DESC")
    List<PaymentHistory> findByBusinessIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("businessId") Long businessId);

    List<PaymentHistory> findByPaymentStatusIn(List<PaymentStatus> statuses);
}
