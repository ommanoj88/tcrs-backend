package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.Business;
import com.tcrs.tcrs_backend.entity.BusinessType;
import com.tcrs.tcrs_backend.entity.IndustryCategory;
import com.tcrs.tcrs_backend.entity.User;
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
public interface BusinessRepository extends JpaRepository<Business, Long> {
    Optional<Business> findByGstin(String gstin);
    Optional<Business> findByPan(String pan);
    List<Business> findByOwner(User owner);
    Boolean existsByGstin(String gstin);
    Boolean existsByPan(String pan);

    @Query("SELECT b FROM Business b WHERE b.owner = ?1 AND b.isActive = true")
    List<Business> findActiveBusinessesByOwner(User owner);

    @Query("SELECT b FROM Business b WHERE b.gstin = ?1 AND b.isActive = true")
    Optional<Business> findActiveBusinessByGstin(String gstin);

    // New search methods
    @Query("SELECT b FROM Business b WHERE b.isActive = true AND " +
            "(LOWER(b.businessName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.gstin) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.pan) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.state) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Business> searchBusinesses(@Param("query") String query, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.isActive = true AND " +
            "(:businessName IS NULL OR LOWER(b.businessName) LIKE LOWER(CONCAT('%', :businessName, '%'))) AND " +
            "(:gstin IS NULL OR LOWER(b.gstin) LIKE LOWER(CONCAT('%', :gstin, '%'))) AND " +
            "(:pan IS NULL OR LOWER(b.pan) LIKE LOWER(CONCAT('%', :pan, '%'))) AND " +
            "(:city IS NULL OR LOWER(b.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:state IS NULL OR LOWER(b.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
            "(:businessType IS NULL OR b.businessType = :businessType) AND " +
            "(:industryCategory IS NULL OR b.industryCategory = :industryCategory)")
    Page<Business> advancedSearchBusinesses(
            @Param("businessName") String businessName,
            @Param("gstin") String gstin,
            @Param("pan") String pan,
            @Param("city") String city,
            @Param("state") String state,
            @Param("businessType") BusinessType businessType,
            @Param("industryCategory") IndustryCategory industryCategory,
            Pageable pageable);

    // Find businesses by location
    Page<Business> findByIsActiveTrueAndCityContainingIgnoreCaseOrStateContainingIgnoreCase(
            String city, String state, Pageable pageable);

    // Find businesses by industry
    Page<Business> findByIsActiveTrueAndIndustryCategory(IndustryCategory industryCategory, Pageable pageable);

    // Find businesses by type
    Page<Business> findByIsActiveTrueAndBusinessType(BusinessType businessType, Pageable pageable);

    // Count total active businesses
    long countByIsActiveTrue();

    // Recently added businesses
    @Query("SELECT b FROM Business b WHERE b.isActive = true ORDER BY b.createdAt DESC")
    Page<Business> findRecentBusinesses(Pageable pageable);


    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByGstinVerifiedTrue();

}