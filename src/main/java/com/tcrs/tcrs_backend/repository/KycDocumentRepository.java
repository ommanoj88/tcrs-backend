package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.KycDocument;
import com.tcrs.tcrs_backend.entity.DocumentType;
import com.tcrs.tcrs_backend.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    List<KycDocument> findByBusinessIdAndDocumentType(Long businessId, DocumentType documentType);

    Optional<KycDocument> findByBusinessIdAndDocumentTypeAndIsPrimaryTrue(Long businessId, DocumentType documentType);

    List<KycDocument> findByVerificationStatus(VerificationStatus status);

    List<KycDocument> findByVerificationStatusOrderByCreatedAtAsc(VerificationStatus status);

    List<KycDocument> findByBusinessIdAndVerificationStatus(Long businessId, VerificationStatus status);

    // Count methods for analytics
    long countByVerificationStatus(VerificationStatus status);

    long countByBusinessId(Long businessId);

    long countByDocumentType(DocumentType documentType);

    // Documents pending verification
    @Query("SELECT d FROM KycDocument d WHERE d.verificationStatus = 'PENDING' ORDER BY d.createdAt ASC")
    List<KycDocument> findPendingDocuments();

    // Documents expiring soon
    @Query("SELECT d FROM KycDocument d WHERE d.expiryDate IS NOT NULL AND d.expiryDate BETWEEN :now AND :futureDate ORDER BY d.expiryDate ASC")
    List<KycDocument> findDocumentsExpiringSoon(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);

    // Verified documents for a business
    @Query("SELECT d FROM KycDocument d WHERE d.business.id = :businessId AND d.verificationStatus = 'VERIFIED' ORDER BY d.verifiedAt DESC")
    List<KycDocument> findVerifiedDocumentsByBusiness(@Param("businessId") Long businessId);

    // Documents by verification officer
    List<KycDocument> findByVerifiedByOrderByVerifiedAtDesc(String verifiedBy);

    // Analytics queries
    @Query("SELECT d.documentType, COUNT(d) FROM KycDocument d GROUP BY d.documentType")
    List<Object[]> countDocumentsByType();

    @Query("SELECT d.verificationStatus, COUNT(d) FROM KycDocument d GROUP BY d.verificationStatus")
    List<Object[]> countDocumentsByStatus();

    @Query("SELECT DATE(d.createdAt), COUNT(d) FROM KycDocument d WHERE d.createdAt >= :fromDate GROUP BY DATE(d.createdAt) ORDER BY DATE(d.createdAt)")
    List<Object[]> getDocumentUploadTrend(@Param("fromDate") LocalDateTime fromDate);
}