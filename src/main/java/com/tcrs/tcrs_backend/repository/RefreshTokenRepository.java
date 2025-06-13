package com.tcrs.tcrs_backend.repository;

import com.tcrs.tcrs_backend.entity.RefreshToken;
import com.tcrs.tcrs_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = ?1 AND rt.isRevoked = false AND rt.expiresAt > ?2")
    Optional<RefreshToken> findValidRefreshToken(String token, LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = ?1")
    void revokeAllUserTokens(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < ?1")
    void deleteExpiredTokens(LocalDateTime now);
}