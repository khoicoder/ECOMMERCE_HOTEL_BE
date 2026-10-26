package com.example.BE.services.Maintenance;

import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.enums.Role;
import com.example.BE.exception.NotFoundException;
import com.example.BE.model.MaintenanceModel;
import com.example.BE.repository.MaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceQueryServiceImpl implements MaintenanceQueryService {

    private final MaintenanceRepository maintenanceRepository;

    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByHotelId(Long hotelId) {
        List<MaintenanceModel> maintenances = maintenanceRepository.findByHotel_Id(hotelId);
        return maintenances.stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<MaintenanceTicketResponse> getAllMaintenance() {
        return maintenanceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByStatus(MaintenanceStatus maintenanceStatus) {
        List<MaintenanceModel> maintenances = maintenanceRepository.findByMaintenanceStatus(maintenanceStatus);
        return maintenances.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByEquipment(Long equipmentId) {
        List<MaintenanceModel> maintenances = maintenanceRepository.findByEquipment_EquipmentId(equipmentId);
        return maintenances.stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByRoom(Long roomId) {
        List<MaintenanceModel> maintenances = maintenanceRepository.findByRoom_Id(roomId);
        return maintenances.stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByAssignedUser(Long userId) {
        List<MaintenanceModel> maintenances = maintenanceRepository.findByAssignedTo_Id(userId);
        return maintenances.stream().map(this::mapToResponse).toList();
    }

    private MaintenanceTicketResponse mapToResponse(MaintenanceModel maintenance) {
        String assignedUsername = "Kỹ sư Kỹ thuật";
        Role assignedRole = Role.ADMIN;

        if (maintenance.getAssignedTo() != null) {
            assignedUsername = maintenance.getAssignedTo().getUsername();
            assignedRole = maintenance.getAssignedTo().getRole();
        }

        return new MaintenanceTicketResponse(
                maintenance.getId(),
                maintenance.getHotel() != null ? maintenance.getHotel().getId() : null,
                maintenance.getRoom() != null ? maintenance.getRoom().getId() : null,
                maintenance.getEquipment() != null ? maintenance.getEquipment().getEquipmentId() : null,
                maintenance.getTitle(),
                maintenance.getDescription(),
                maintenance.getMaintenanceStatus(),
                maintenance.getMaintenanceType(),
                maintenance.getCreatedBy(),
                maintenance.getCreatedAt(),
                maintenance.getUpdatedAt(),
                assignedUsername,
                assignedRole
        );
    }
}
