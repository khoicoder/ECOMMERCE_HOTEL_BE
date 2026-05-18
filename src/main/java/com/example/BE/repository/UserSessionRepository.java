package com.example.BE.repository;

import com.example.BE.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("""
        update UserSession s
        set s.revokedAt = :now
        where s.user.id = :userId
        and s.id <> :currentSessionId
        and s.revokedAt is null
    """)
    int revokeAllActiveSessionsExcept(
            Long userId,
            UUID currentSessionId,
            Instant now
    );

    @Modifying
    @Query("""
        update UserSession s
        set s.revokedAt = :now
        where s.user.id = :userId
        and s.revokedAt is null
    """)
    int revokeAllActiveSessions(
            Long userId,
            Instant now
    );
}