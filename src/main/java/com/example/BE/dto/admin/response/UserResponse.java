package com.example.BE.dto.admin.response;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role,
        String phone,
        String address,
        boolean active,
        Instant createdAt,
        Instant updateAt

){}
