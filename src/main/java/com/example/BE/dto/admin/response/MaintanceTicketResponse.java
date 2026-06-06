package com.example.BE.dto.admin.response;

import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.enums.MaintenanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public record MaintanceTicketResponse(
        Long id,
        Long hotelId,
        Long roomId,
        Long equipmentId,
        String title,
        String description,
        MaintenanceStatus status,
        MaintenanceType Type,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

}
