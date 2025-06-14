package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.User;
import com.tcrs.tcrs_backend.entity.UserRoleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleHistoryRepository extends JpaRepository<UserRoleHistory, Long> {

    List<UserRoleHistory> findByUserOrderByChangedAtDesc(User user);

    @Query("SELECT urh FROM UserRoleHistory urh ORDER BY urh.changedAt DESC")
    List<UserRoleHistory> findAllOrderByChangedAtDesc();

    @Query("SELECT urh FROM UserRoleHistory urh WHERE urh.changedBy = ?1 ORDER BY urh.changedAt DESC")
    List<UserRoleHistory> findByChangedByOrderByChangedAtDesc(User changedBy);
}
