package kr.co.gymleco.domain.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface AdminRefreshTokenRepository extends JpaRepository<AdminRefreshToken, Long> {

    Optional<AdminRefreshToken> findByTokenHash(byte[] tokenHash);

    @Modifying
    @Query("""
         UPDATE AdminRefreshToken t
              SET t.revokedAt = :now
            WHERE t.adminUser.id = :userId
              AND t.revokedAt IS NULL
           """)
    int revokeAllForUser(@Param("userId") Long userId, @Param("now") Instant now);
    @Modifying
    @Query("DELETE FROM AdminRefreshToken t WHERE t.expiresAt < :threshold")
    int deleteExpiredBefore(@Param("threshold") Instant threshold);
}
