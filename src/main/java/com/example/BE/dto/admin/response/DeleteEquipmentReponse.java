package com.example.BE.dto.admin.response;

import com.example.BE.enums.Role;

import java.time.Instant;

public record DeleteEquipmentReponse(
        Long id,
        String nameItem,
        Integer quantity,
        Role role,
        Long userId,
        Instant createdAt

) {
}
