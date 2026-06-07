package com.example.BE.dto.admin.response;

import com.example.BE.enums.EquipmentStatus;

import java.time.LocalDateTime;

public record EquipmentResponse(
        Long id,
        String name,
        String brand,
        String serialNumber,
        EquipmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long hotelId,
        Long roomId,
        String note

){

}
