package com.tcrs.tcrs_backend.repository;


import com.tcrs.tcrs_backend.entity.AnalyticsSnapshot;
import com.tcrs.tcrs_backend.entity.SnapShotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsSnapshot, Long> {

    List<AnalyticsSnapshot> findBySnapshotTypeOrderBySnapshotDateDesc(SnapShotType snapshotType);

    List<AnalyticsSnapshot> findBySnapshotTypeAndSnapshotDateBetweenOrderBySnapshotDateDesc(
            SnapShotType snapshotType, LocalDateTime startDate, LocalDateTime endDate);

    Optional<AnalyticsSnapshot> findTopBySnapshotTypeOrderBySnapshotDateDesc(SnapShotType snapshotType);

    @Query("SELECT a FROM AnalyticsSnapshot a WHERE a.snapshotType = :type AND a.snapshotDate >= :fromDate ORDER BY a.snapshotDate DESC")
    List<AnalyticsSnapshot> findRecentSnapshots(@Param("type") SnapShotType type, @Param("fromDate") LocalDateTime fromDate);
}