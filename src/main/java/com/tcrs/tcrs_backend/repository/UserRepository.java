package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Boolean existsByEmail(String email);

    List<User> findAll(Sort sort);

    Boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.isActive = true")
    Optional<User> findActiveUserByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.phone = ?1 AND u.isActive = true")
    Optional<User> findActiveUserByPhone(String phone);
}