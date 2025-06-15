package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeReferenceRepository extends JpaRepository<TradeReference, Long> {

    // Basic Queries
    List<TradeReference> findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(Business business);

    Page<TradeReference> findByBusinessAndIsActiveTrueOrderByCreatedAtDesc(Business business, Pageable pageable);

    List<TradeReference> findByAddedByAndIsActiveTrueOrderByCreatedAtDesc(User addedBy);

    Page<TradeReference> findByAddedByAndIsActiveTrueOrderByCreatedAtDesc(User addedBy, Pageable pageable);

    Optional<TradeReference> findByReferenceNumberAndIsActiveTrue(String referenceNumber);

    // Verification Status Queries
    List<TradeReference> findByVerificationStatusAndIsActiveTrueOrderByCreatedAtDesc(
            ReferenceVerificationStatus verificationStatus);

    Page<TradeReference> findByVerificationStatusAndIsActiveTrueOrderByCreatedAtDesc(
            ReferenceVerificationStatus verificationStatus, Pageable pageable);

    @Query("SELECT tr FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.verificationStatus = :status ORDER BY tr.createdAt DESC")
    List<TradeReference> findByBusinessAndVerificationStatus(@Param("business") Business business,
                                                             @Param("status") ReferenceVerificationStatus status);

    // Reference Type and Relationship Queries
    List<TradeReference> findByBusinessAndReferenceTypeAndIsActiveTrueOrderByCreatedAtDesc(
            Business business, ReferenceType referenceType);

    List<TradeReference> findByBusinessAndRelationshipTypeAndIsActiveTrueOrderByCreatedAtDesc(
            Business business, RelationshipType relationshipType);

    // Contact Information Queries
    List<TradeReference> findByEmailAndIsActiveTrue(String email);

    List<TradeReference> findByPhoneAndIsActiveTrue(String phone);

    List<TradeReference> findByCompanyGstinAndIsActiveTrue(String companyGstin);

    // Analytics Queries
    @Query("SELECT COUNT(tr) FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true")
    Long countTotalReferencesByBusiness(@Param("business") Business business);

    @Query("SELECT COUNT(tr) FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.verificationStatus = 'VERIFIED'")
    Long countVerifiedReferencesByBusiness(@Param("business") Business business);

    @Query("SELECT COUNT(tr) FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.recommendationLevel IN ('HIGHLY_RECOMMENDED', 'RECOMMENDED')")
    Long countPositiveReferencesByBusiness(@Param("business") Business business);

    @Query("SELECT COUNT(tr) FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.recommendationLevel = 'NOT_RECOMMENDED'")
    Long countNegativeReferencesByBusiness(@Param("business") Business business);

    @Query("SELECT AVG(CAST(tr.paymentRating AS double)) FROM TradeReference tr WHERE tr.business = :business " +
            "AND tr.isActive = true AND tr.paymentRating IS NOT NULL AND tr.verificationStatus = 'VERIFIED'")
    Double averagePaymentRatingByBusiness(@Param("business") Business business);

    @Query("SELECT AVG(CAST(tr.overallRating AS double)) FROM TradeReference tr WHERE tr.business = :business " +
            "AND tr.isActive = true AND tr.overallRating IS NOT NULL AND tr.verificationStatus = 'VERIFIED'")
    Double averageOverallRatingByBusiness(@Param("business") Business business);

    @Query("SELECT SUM(tr.totalBusinessValue) FROM TradeReference tr WHERE tr.business = :business " +
            "AND tr.isActive = true AND tr.totalBusinessValue IS NOT NULL AND tr.verificationStatus = 'VERIFIED'")
    BigDecimal sumTotalBusinessValueByBusiness(@Param("business") Business business);

    @Query("SELECT SUM(tr.averageMonthlyBusiness) FROM TradeReference tr WHERE tr.business = :business " +
            "AND tr.isActive = true AND tr.averageMonthlyBusiness IS NOT NULL AND tr.verificationStatus = 'VERIFIED'")
    BigDecimal sumAverageMonthlyBusinessByBusiness(@Param("business") Business business);

    // Reference Distribution Queries
    @Query("SELECT tr.referenceType, COUNT(tr) FROM TradeReference tr WHERE tr.business = :business " +
            "AND tr.isActive = true GROUP BY tr.referenceType")
    List<Object[]> getReferenceTypeDistribution(@Param("business") Business business);

    @Query("SELECT tr.relationshipType, COUNT(tr) FROM TradeReference tr WHERE tr.business = :business " +
            "AND tr.isActive = true GROUP BY tr.relationshipType")
    List<Object[]> getRelationshipTypeDistribution(@Param("business") Business business);

    @Query("SELECT tr.paymentBehavior, COUNT(tr) FROM TradeReference tr WHERE tr.business = :business " +
            "AND tr.isActive = true AND tr.verificationStatus = 'VERIFIED' GROUP BY tr.paymentBehavior")
    List<Object[]> getPaymentBehaviorDistribution(@Param("business") Business business);

    @Query("SELECT tr.recommendationLevel, COUNT(tr) FROM TradeReference tr WHERE tr.business = :business " +
            "AND tr.isActive = true AND tr.verificationStatus = 'VERIFIED' GROUP BY tr.recommendationLevel")
    List<Object[]> getRecommendationLevelDistribution(@Param("business") Business business);

    // Date Range Queries
    @Query("SELECT tr FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.createdAt BETWEEN :startDate AND :endDate ORDER BY tr.createdAt DESC")
    List<TradeReference> findByBusinessAndDateRange(@Param("business") Business business,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // Verification Follow-up Queries
    @Query("SELECT tr FROM TradeReference tr WHERE tr.verificationStatus = 'PENDING' " +
            "AND tr.isActive = true AND tr.createdAt < :cutoffDate ORDER BY tr.createdAt ASC")
    List<TradeReference> findPendingReferencesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT tr FROM TradeReference tr WHERE tr.verificationStatus = 'PENDING' " +
            "AND tr.isActive = true AND tr.contactAttemptedDate IS NULL ORDER BY tr.createdAt ASC")
    List<TradeReference> findPendingReferencesNotContacted();

    // Duplicate Detection Queries
    @Query("SELECT tr FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND (tr.email = :email OR tr.phone = :phone OR " +
            "(tr.companyGstin IS NOT NULL AND tr.companyGstin = :gstin)) " +
            "AND tr.id != :excludeId")
    List<TradeReference> findPotentialDuplicates(@Param("business") Business business,
                                                 @Param("email") String email,
                                                 @Param("phone") String phone,
                                                 @Param("gstin") String gstin,
                                                 @Param("excludeId") Long excludeId);

    // Reference Provider Queries (when referenceProvider is linked)
    List<TradeReference> findByReferenceProviderAndIsActiveTrueOrderByCreatedAtDesc(Business referenceProvider);

    @Query("SELECT tr FROM TradeReference tr WHERE tr.referenceProvider = :provider AND tr.isActive = true " +
            "AND tr.verificationStatus = 'VERIFIED' ORDER BY tr.createdAt DESC")
    List<TradeReference> findVerifiedReferencesByProvider(@Param("provider") Business provider);

    // High Value References
    @Query("SELECT tr FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.totalBusinessValue >= :minValue ORDER BY tr.totalBusinessValue DESC")
    List<TradeReference> findHighValueReferences(@Param("business") Business business,
                                                 @Param("minValue") BigDecimal minValue);

    // Long-term Relationships
    @Query("SELECT tr FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.relationshipDurationMonths >= :minMonths ORDER BY tr.relationshipDurationMonths DESC")
    List<TradeReference> findLongTermReferences(@Param("business") Business business,
                                                @Param("minMonths") Integer minMonths);

    // Recent Verification Activity
    @Query("SELECT tr FROM TradeReference tr WHERE tr.verificationStatus = 'VERIFIED' " +
            "AND tr.verifiedDate >= :since ORDER BY tr.verifiedDate DESC")
    List<TradeReference> findRecentlyVerifiedReferences(@Param("since") LocalDateTime since);

    // Confidential References
    @Query("SELECT COUNT(tr) FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.isConfidential = true")
    Long countConfidentialReferencesByBusiness(@Param("business") Business business);

    // Dispute Analysis
    @Query("SELECT COUNT(tr) FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND tr.hasDisputes = true AND tr.verificationStatus = 'VERIFIED'")
    Long countReferencesWithDisputesByBusiness(@Param("business") Business business);

    // Search functionality
    @Query("SELECT tr FROM TradeReference tr WHERE tr.business = :business AND tr.isActive = true " +
            "AND (LOWER(tr.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tr.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tr.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "tr.phone LIKE CONCAT('%', :searchTerm, '%')) ORDER BY tr.createdAt DESC")
    List<TradeReference> searchReferences(@Param("business") Business business,
                                          @Param("searchTerm") String searchTerm);

    // Add this method to your existing TradeReferenceRepository.java:

    List<TradeReference> findByBusinessIdAndIsActiveTrue(Long businessId);
}