package com.example.BE.repository;

import com.example.BE.dto.admin.response.SessionResponse;
import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
    Optional<UserSession> findByIdAndRevokedAtIsNull(UUID id);
    List<UserSession> findByUser(UserModel user);
    List<UserSession> findByUserId(Long id);

    @Modifying
    @Query("""
        update UserSession s
        set s.revokedAt = :now
        where s.user.id = :userId
        and s.id <> :currentSessionId
        and s.revokedAt is null 
    """)
            //soft revoke
// chỉ revoke session còn active.
//    and s.id <> :currentSessionId => trừ session hiện tại.
    int revokeAllActiveSessionsExcept(
            Long userId,
            UUID currentSessionId,
            Instant now
    );
    //đánh dấu session đã revoke.
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

    @Query("""
    select count(s)
    from UserSession s
    where s.revokedAt is null
""")
    long countActiveSessions();
}