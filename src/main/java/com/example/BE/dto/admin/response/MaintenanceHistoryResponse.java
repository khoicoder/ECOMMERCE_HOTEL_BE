package com.example.BE.dto.admin.response;

import com.example.BE.enums.MaintenanceStatus;

public record MaintenanceHistoryResponse (
        Long id,
        Long maintenanceId,
        MaintenanceStatus oldStatus,
        MaintenanceStatus newStatus,
        String changedBy,
        String changeAt

){
}
