package com.example.BE.services;

import com.example.BE.dto.admin.request.CreateMaintenanceTicketRequest;
import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.EquipmentStatus;
import com.example.BE.enums.MaintenanceStatus;

import java.util.List;

public interface MaintenanceService {
    List<MaintenanceTicketResponse> getMaintenanceByHotelId(Long hotelId);
    MaintenanceTicketResponse createMaintenance(CreateMaintenanceTicketRequest request);
    List<MaintenanceTicketResponse> getAllMaintenance();
    List<MaintenanceTicketResponse> getByHotelId(Long hotelId);
    MaintenanceTicketResponse updateMaintenanceStatus(Long hotelId,Long Maintenanceid, MaintenanceStatus status);
    void delete(Long id);
}
