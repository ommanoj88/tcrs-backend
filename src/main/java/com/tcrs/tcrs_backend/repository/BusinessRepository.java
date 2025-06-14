package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.Business;
import com.tcrs.tcrs_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}