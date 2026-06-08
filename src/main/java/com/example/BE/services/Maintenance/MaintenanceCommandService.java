package com.example.BE.services.Maintenance;

import com.example.BE.dto.admin.request.CreateMaintenanceTicketRequest;
import com.example.BE.dto.admin.request.UpdateMaintenaceRequest;
import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.MaintenanceStatus;
import org.springframework.security.core.Authentication;

//tạo/sửa/xóa, đổi trạng thái.
public interface MaintenanceCommandService {
    MaintenanceTicketResponse createMaintenance(CreateMaintenanceTicketRequest request, Authentication authentication);
    MaintenanceTicketResponse updateMaintenance(Long maintenanceId,UpdateMaintenaceRequest request);
    MaintenanceTicketResponse updateMaintenanceByHotelId(Long hotelId,Long maintenanceId,UpdateMaintenaceRequest request);
    MaintenanceTicketResponse updateMaintenanceStatus(Long hotelId, Long maintenanceId, MaintenanceStatus status);
    MaintenanceTicketResponse reopenMaintenance(Long maintenanceId);
    void delete(Long id);
}
