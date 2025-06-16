package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.KycProfile;
import com.tcrs.tcrs_backend.entity.KycStatus;
import com.tcrs.tcrs_backend.entity.RiskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {

    Optional<KycProfile> findByBusinessId(Long businessId);

    List<KycProfile> findByKycStatus(KycStatus status);

    List<KycProfile> findByRiskCategory(RiskCategory category);

    List<KycProfile> findByAssignedOfficer(String officer);

    // KYC profiles needing review
    @Query("SELECT k FROM KycProfile k WHERE k.nextReviewDate IS NOT NULL AND k.nextReviewDate <= :reviewDate ORDER BY k.nextReviewDate ASC")
    List<KycProfile> findProfilesNeedingReview(@Param("reviewDate") LocalDateTime reviewDate);

    // High-risk profiles
    @Query("SELECT k FROM KycProfile k WHERE k.riskCategory = 'HIGH' OR k.riskCategory = 'VERY_HIGH' ORDER BY k.riskScore DESC")
    List<KycProfile> findHighRiskProfiles();

    // Completed profiles by date range
    @Query("SELECT k FROM KycProfile k WHERE k.kycStatus = 'COMPLETED' AND k.lastVerificationDate BETWEEN :startDate AND :endDate")
    List<KycProfile> findCompletedProfilesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count methods for analytics
    long countByKycStatus(KycStatus status);

    long countByRiskCategory(RiskCategory category);

    @Query("SELECT AVG(k.completionPercentage) FROM KycProfile k")
    Double getAverageCompletionPercentage();

    @Query("SELECT AVG(k.riskScore) FROM KycProfile k WHERE k.riskScore IS NOT NULL")
    Double getAverageRiskScore();

    // Analytics queries
    @Query("SELECT k.kycStatus, COUNT(k) FROM KycProfile k GROUP BY k.kycStatus")
    List<Object[]> countProfilesByStatus();

    @Query("SELECT k.riskCategory, COUNT(k) FROM KycProfile k GROUP BY k.riskCategory")
    List<Object[]> countProfilesByRiskCategory();
}