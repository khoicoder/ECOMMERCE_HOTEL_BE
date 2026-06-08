package com.example.BE.services.Maintenance;

import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.MaintenanceStatus;
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
        List<MaintenanceModel> maintenances = maintenanceRepository.findByHotelId(hotelId);
        if (maintenances.isEmpty()) {
            throw new NotFoundException("Maintenance not found for hotelId = " + hotelId);
        }
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
        List<MaintenanceModel> maintenances = maintenanceRepository.findByStatus(maintenanceStatus);
        if (maintenances.isEmpty()) {
            throw new NotFoundException("No maintenance found with status = " + maintenanceStatus);
        }
        return maintenances.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByEquipment(Long equipmentId) {
        List<MaintenanceModel> maintenances = maintenanceRepository.findByEquipmentId(equipmentId);
        if (maintenances.isEmpty()) {
            throw new NotFoundException("No maintenance found for equipmentId = " + equipmentId);
        }
        return maintenances.stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByRoom(Long roomId) {
        List<MaintenanceModel> maintenances = maintenanceRepository.findByRoomId(roomId);
        if (maintenances.isEmpty()) {
            throw new NotFoundException("No maintenance found for roomId = " + roomId);
        }
        return maintenances.stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByAssignedUser(Long userId) {
        List<MaintenanceModel> maintenances = maintenanceRepository.findByAssignedUserId(userId);
        if (maintenances.isEmpty()) {
            throw new NotFoundException("No maintenance found for assigned userId = " + userId);
        }
        return maintenances.stream().map(this::mapToResponse).toList();
    }

    private MaintenanceTicketResponse mapToResponse(MaintenanceModel maintenance) {
        return new MaintenanceTicketResponse(
                maintenance.getId(),
                maintenance.getHotel() != null ? maintenance.getHotel().getId() : null,
                maintenance.getRoom() != null ? maintenance.getRoom().getId() : null,
                maintenance.getEquipment() != null ? maintenance.getEquipment().getEquipmentId() : null,
                maintenance.getTitle(),
                maintenance.getDescription(),
                maintenance.getMaintenanceStatus(),
                maintenance.getMaintenanceType(),
                maintenance.getCreatedBy() != null ? maintenance.getCreatedBy().getId() : null,
                maintenance.getCreatedAt(),
                maintenance.getUpdatedAt(),
                maintenance.getCreatedBy() != null ? maintenance.getCreatedBy().getUsername() : null,
                maintenance.getCreatedBy() != null && maintenance.getCreatedBy().getRole() != null
                        ? maintenance.getCreatedBy().getRole().name()
                        : null
        );
    }
}