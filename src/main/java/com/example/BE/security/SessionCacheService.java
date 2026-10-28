package com.example.BE.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 * High-performance Redis session cache layer.
 * Acts as a fast-path cache in front of MySQL authoritative storage.
 * All Redis operations are guarded with safe fallback mechanisms.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public static final String SESSION_KEY_PREFIX = "auth:session:";
    public static final String VALUE_REVOKED = "REVOKED";
    private static final Duration REVOKED_TTL = Duration.ofMinutes(15); // Covers max access token validity window

    /**
     * Checks if a session is valid according to the Redis cache.
     *
     * @param sessionId the UUID of the session
     * @return SessionCacheStatus (VALID, REVOKED, MISS, or ERROR)
     */
    public SessionCacheStatus isSessionValid(UUID sessionId) {
        if (sessionId == null) {
            return SessionCacheStatus.REVOKED;
        }

        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                return SessionCacheStatus.MISS;
            }
            if (VALUE_REVOKED.equalsIgnoreCase(value)) {
                return SessionCacheStatus.REVOKED;
            }
            if (value.startsWith("ACTIVE:")) {
                return SessionCacheStatus.VALID;
            }
            return SessionCacheStatus.MISS;
        } catch (Exception ex) {
            log.warn("[SessionCacheService] Redis connection error during session check for {}: {}. Triggering safe database fallback.",
                    sessionId, ex.getMessage());
            return SessionCacheStatus.ERROR;
        }
    }

    /**
     * Caches an active session in Redis with a TTL matching session expiration.
     */
    public void cacheSession(UUID sessionId, Long userId, String role, String tenantCode, Instant expireAt) {
        if (sessionId == null) return;

        String key = SESSION_KEY_PREFIX + sessionId;
        String value = "ACTIVE:" + userId + ":" + (role != null ? role : "USER") + ":" + (tenantCode != null ? tenantCode : "DEFAULT");

        Duration ttl = Duration.between(Instant.now(), expireAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        try {
            stringRedisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception ex) {
            log.warn("[SessionCacheService] Failed to cache session in Redis for {}: {}", sessionId, ex.getMessage());
        }
    }

    /**
     * Marks a session as REVOKED in Redis so subsequent requests are rejected immediately.
     */
    public void revokeSession(UUID sessionId) {
        if (sessionId == null) return;

        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            stringRedisTemplate.opsForValue().set(key, VALUE_REVOKED, REVOKED_TTL);
        } catch (Exception ex) {
            log.warn("[SessionCacheService] Failed to mark session revoked in Redis for {}: {}", sessionId, ex.getMessage());
        }
    }

    /**
     * Revokes multiple sessions (e.g., on logout-all or admin account lock).
     */
    public void revokeSessions(Collection<UUID> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return;

        for (UUID sid : sessionIds) {
            revokeSession(sid);
        }
    }
}
