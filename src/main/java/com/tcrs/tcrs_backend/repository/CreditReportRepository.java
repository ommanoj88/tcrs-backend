package com.tcrs.tcrs_backend.repository;


import com.tcrs.tcrs_backend.entity.Business;
import com.tcrs.tcrs_backend.entity.CreditReport;
import com.tcrs.tcrs_backend.entity.ReportStatus;
import com.tcrs.tcrs_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditReportRepository extends JpaRepository<CreditReport, Long> {

    Optional<CreditReport> findByReportNumber(String reportNumber);

    List<CreditReport> findByBusinessOrderByCreatedAtDesc(Business business);

    List<CreditReport> findByRequestedByOrderByCreatedAtDesc(User requestedBy);

    Page<CreditReport> findByRequestedByOrderByCreatedAtDesc(User requestedBy, Pageable pageable);

    @Query("SELECT cr FROM CreditReport cr WHERE cr.business = ?1 AND cr.reportStatus = ?2 " +
            "AND cr.reportValidUntil > ?3 ORDER BY cr.createdAt DESC")
    Optional<CreditReport> findValidReportByBusiness(Business business, ReportStatus status, LocalDateTime now);

    @Query("SELECT cr FROM CreditReport cr WHERE cr.reportValidUntil < ?1 AND cr.reportStatus = ?2")
    List<CreditReport> findExpiredReports(LocalDateTime now, ReportStatus status);

    @Query("SELECT COUNT(cr) FROM CreditReport cr WHERE cr.business = ?1")
    Long countReportsByBusiness(Business business);

    @Query("SELECT COUNT(cr) FROM CreditReport cr WHERE cr.requestedBy = ?1")
    Long countReportsByUser(User user);

    @Query("SELECT cr FROM CreditReport cr WHERE cr.business.gstin = ?1 ORDER BY cr.createdAt DESC")
    List<CreditReport> findByBusinessGstinOrderByCreatedAtDesc(String gstin);
}