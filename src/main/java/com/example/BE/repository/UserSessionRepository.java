package com.example.BE.repository;

import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
    Optional<UserSession> findByIdAndRevokedAtIsNull(UUID id);
    List<UserSession> findByUser(UserModel user);
    List<UserSession> findByUserId(Long id);

    long countByRevokedAtIsNull();
    long countByUserIdAndRevokedAtIsNull(Long userId);

    @Modifying
    @Query("""
        update UserSession s
        set s.revokedAt = :now
        where s.user.id = :userId
        and s.id <> :currentSessionId
        and s.revokedAt is null 
    """)
    int revokeAllActiveSessionsExcept(
            @Param("userId") Long userId,
            @Param("currentSessionId") UUID currentSessionId,
            @Param("now") Instant now
    );

    @Modifying
    @Query(""" 
        update UserSession s
        set s.revokedAt = :now  
        where s.user.id = :userId
        and s.revokedAt is null
    """)
    int revokeAllActiveSessions(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        update UserSession s
        set s.refreshTokenHash = :newHash,
            s.refreshTokenExpireAt = :newExpireAt,
            s.lastUsedAt = :now
        where s.id = :sessionId
        and s.refreshTokenHash = :oldHash
        and s.revokedAt is null
        and s.refreshTokenExpireAt > :now
    """)
    int rotateRefreshToken(
            @Param("sessionId") UUID sessionId,
            @Param("oldHash") String oldHash,
            @Param("newHash") String newHash,
            @Param("newExpireAt") Instant newExpireAt,
            @Param("now") Instant now
    );
}
