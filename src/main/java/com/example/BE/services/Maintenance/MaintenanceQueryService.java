package com.example.BE.services.Maintenance;

import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.MaintenanceStatus;

import java.util.List;

public interface MaintenanceQueryService {
    List<MaintenanceTicketResponse> getMaintenanceByHotelId(Long hotelId);
    List<MaintenanceTicketResponse> getAllMaintenance();
    List<MaintenanceTicketResponse> getMaintenanceByStatus(MaintenanceStatus maintenanceStatus);
    List<MaintenanceTicketResponse> getMaintenanceByEquipment(Long equipmentId);
    List<MaintenanceTicketResponse> getMaintenanceByRoom(Long roomId);
    List<MaintenanceTicketResponse> getMaintenanceByAssignedUser(Long userId);
}
