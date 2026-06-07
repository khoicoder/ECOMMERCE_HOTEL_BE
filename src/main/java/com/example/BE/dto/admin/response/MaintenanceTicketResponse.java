package com.example.BE.dto.admin.response;

import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.enums.MaintenanceType;

import com.example.BE.model.UserModel;


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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdByName,
        String createdByRole
) {

}
