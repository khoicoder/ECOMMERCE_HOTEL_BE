package com.example.BE.security;

/**
 * Result status when querying the Redis session cache.
 */
public enum SessionCacheStatus {
    VALID,      // Found in Redis cache and marked ACTIVE
    REVOKED,    // Found in Redis cache and marked REVOKED
    MISS,       // Key does not exist in Redis (requires MySQL check & cache warm-up)
    ERROR       // Redis offline, timed out, or connection failed (safe MySQL fallback required)
}
