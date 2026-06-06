package com.example.BE.dto.admin.response;

import com.example.BE.enums.EquipmentStatus;

public record EquipmentResponse(
        Long id,
        String name,
        String brand,
        String serialNumber,
        EquipmentStatus status,
        Long hotelId,
        Long roomId,
        String note

){

}
