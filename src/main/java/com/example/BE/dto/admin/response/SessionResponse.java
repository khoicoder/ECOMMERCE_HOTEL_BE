package com.example.BE.dto.admin.response;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID sessionId,
        Instant createdAt,
        Instant lastUsedAt,
        boolean active

){
}
