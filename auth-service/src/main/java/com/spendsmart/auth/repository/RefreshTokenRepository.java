// com/spendsmart/auth/repository/RefreshTokenRepository.java
package com.spendsmart.auth.repository;

import com.spendsmart.auth.entity.RefreshToken;
import com.spendsmart.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing {@link RefreshToken} entities.
 * Provides custom queries for token revocation and cleanup.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Revoke all tokens for a user (used on logout)
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user")
    void revokeAllUserTokens(@Param("user") User user);

    // Clean up expired/revoked tokens (scheduled job can call this)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true OR rt.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredAndRevokedTokens();
}