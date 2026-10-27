package com.example.BE.security;

import com.example.BE.enums.Role;

import java.util.UUID;

public record AuthPrincipal(
        Long userId,
        String username,
        Role role,
        UUID sessionId,
        String tenantCode
) {
    public AuthPrincipal(Long userId, String username, Role role, UUID sessionId) {
        this(userId, username, role, sessionId, "DEFAULT");
    }
}
