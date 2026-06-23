package com.example.BE.dto.admin.response;

import com.example.BE.enums.HotelStaffPosition;
import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.enums.MaintenanceType;

import com.example.BE.enums.Role;
import com.example.BE.model.UserModel;


import java.time.Instant;
import java.time.LocalDateTime;

public record MaintenanceTicketResponse(
        Long id,
        Long hotelId,
        Long roomId,
        Long equipmentId,
        String title,
        String description,
        MaintenanceStatus status,
        MaintenanceType type,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt,
        String createdByName,
        Role createdByRole
) {

}
