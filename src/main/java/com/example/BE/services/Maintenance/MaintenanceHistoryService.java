package com.example.BE.services.Maintenance;

import com.example.BE.dto.admin.response.MaintenanceHistoryResponse;

import java.util.List;

public interface MaintenanceHistoryService {
    List<MaintenanceHistoryResponse> getMaintenanceHistory(Long maintenanceId);
}
