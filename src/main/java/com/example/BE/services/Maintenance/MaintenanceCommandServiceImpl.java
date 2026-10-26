package com.example.BE.services.Maintenance;

import com.example.BE.dto.admin.request.CreateMaintenanceTicketRequest;
import com.example.BE.dto.admin.request.UpdateMaintenaceRequest;
import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.enums.Role;
import com.example.BE.enums.RoomStatus;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.model.EquipmentModel;
import com.example.BE.model.HotelModel;
import com.example.BE.model.MaintenanceModel;
import com.example.BE.model.RoomModel;
import com.example.BE.model.UserModel;
import com.example.BE.repository.EquipmentRepository;
import com.example.BE.repository.HotelRepository;
import com.example.BE.repository.MaintenanceRepository;
import com.example.BE.repository.RoomRepository;
import com.example.BE.repository.UserRepository;
import com.example.BE.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MaintenanceCommandServiceImpl implements MaintenanceCommandService {

    private final MaintenanceRepository maintenanceRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MaintenanceTicketResponse updateMaintenanceByHotelId(Long hotelId, Long maintenanceId, UpdateMaintenaceRequest request) {
        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() ->
                new NotFoundException("Maintenance with id: " + maintenanceId));

        if (maintenance.getHotel() != null && !maintenance.getHotel().getId().equals(hotelId)) {
            throw new BadRequestException("Maintenance doesn't belong to this hotel " + hotelId);
        }

        if (request.title() != null && !request.title().isBlank()) {
            maintenance.setTitle(request.title());
        }
        if (request.description() != null && !request.description().isBlank()) {
            maintenance.setDescription(request.description());
        }
        if (request.assignedToId() != null) {
            UserModel assignedUser = userRepository.findById(request.assignedToId()).orElse(null);
            maintenance.setAssignedTo(assignedUser);
        }
        if (request.roomId() != null) {
            RoomModel room = roomRepository.findById(request.roomId()).orElse(null);
            if (room != null) {
                maintenance.setRoom(room);
            }
        }
        maintenance.setUpdatedAt(Instant.now());
        MaintenanceModel saved = maintenanceRepository.save(maintenance);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceTicketResponse updateMaintenance(Long maintenanceId, UpdateMaintenaceRequest request) {
        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() ->
                new NotFoundException("Maintenance with id: " + maintenanceId));

        if (request.title() != null && !request.title().isBlank()) {
            maintenance.setTitle(request.title());
        }
        if (request.description() != null && !request.description().isBlank()) {
            maintenance.setDescription(request.description());
        }
        if (request.assignedToId() != null) {
            UserModel assignedUser = userRepository.findById(request.assignedToId()).orElse(null);
            maintenance.setAssignedTo(assignedUser);
        }
        if (request.roomId() != null) {
            RoomModel room = roomRepository.findById(request.roomId()).orElse(null);
            if (room != null) {
                maintenance.setRoom(room);
            }
        }
        maintenance.setUpdatedAt(Instant.now());
        MaintenanceModel saved = maintenanceRepository.save(maintenance);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceTicketResponse createMaintenance(CreateMaintenanceTicketRequest request, Authentication authentication) {
        Long creatorId = request.getCreatedById();
        if (creatorId == null && authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            creatorId = principal.userId();
        }

        HotelModel hotel = null;
        if (request.getHotelId() != null) {
            hotel = hotelRepository.findById(request.getHotelId()).orElse(null);
        }

        MaintenanceModel maintenance = new MaintenanceModel();
        maintenance.setHotel(hotel);
        maintenance.setCreatedBy(creatorId);
        maintenance.setCreatedAt(Instant.now());
        maintenance.setUpdatedAt(Instant.now());
        maintenance.setTitle(request.getTitle() != null ? request.getTitle() : "Yêu cầu bảo trì sự cố");
        maintenance.setDescription(request.getDescription());
        maintenance.setMaintenanceStatus(MaintenanceStatus.PENDING);

        if (request.getRoomId() != null) {
            RoomModel room = roomRepository.findById(request.getRoomId()).orElse(null);
            if (room != null) {
                maintenance.setRoom(room);
            }
        }

        if (request.getEquipmentId() != null) {
            EquipmentModel equipment = equipmentRepository.findById(request.getEquipmentId()).orElse(null);
            maintenance.setEquipment(equipment);
        }

        return mapToResponse(maintenanceRepository.save(maintenance));
    }

    @Override
    @Transactional
    public MaintenanceTicketResponse updateMaintenanceStatus(Long hotelId, Long maintenanceId, MaintenanceStatus newStatus) {
        if (maintenanceId == null) {
            throw new BadRequestException("Maintenance ID cannot be null");
        }

        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new NotFoundException("Maintenance not found with id = " + maintenanceId));

        return applyStatusChange(maintenance, newStatus);
    }

    @Override
    @Transactional
    public MaintenanceTicketResponse updateMaintenanceStatusDirect(Long maintenanceId, MaintenanceStatus newStatus) {
        if (maintenanceId == null) {
            throw new BadRequestException("Maintenance ID cannot be null");
        }

        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new NotFoundException("Maintenance not found with id = " + maintenanceId));

        return applyStatusChange(maintenance, newStatus);
    }

    private MaintenanceTicketResponse applyStatusChange(MaintenanceModel maintenance, MaintenanceStatus newStatus) {
        if (newStatus == null) {
            newStatus = MaintenanceStatus.IN_PROGRESS;
        }

        // Canonical mapping
        MaintenanceStatus target = newStatus;
        if (target == MaintenanceStatus.RESOLVED) target = MaintenanceStatus.COMPLETED;
        if (target == MaintenanceStatus.OPEN) target = MaintenanceStatus.PENDING;

        maintenance.setMaintenanceStatus(target);
        maintenance.setUpdatedAt(Instant.now());

        if (target == MaintenanceStatus.COMPLETED) {
            maintenance.setCompletedAt(Instant.now());
            // If room was under maintenance, return to AVAILABLE
            if (maintenance.getRoom() != null && maintenance.getRoom().getStatus() == RoomStatus.MAINTENANCE) {
                maintenance.getRoom().setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(maintenance.getRoom());
            }
        } else if (target == MaintenanceStatus.IN_PROGRESS) {
            // If room is not occupied, mark as MAINTENANCE
            if (maintenance.getRoom() != null && maintenance.getRoom().getStatus() != RoomStatus.OCCUPIED) {
                maintenance.getRoom().setStatus(RoomStatus.MAINTENANCE);
                roomRepository.save(maintenance.getRoom());
            }
        }

        return mapToResponse(maintenanceRepository.save(maintenance));
    }

    @Override
    @Transactional
    public MaintenanceTicketResponse reopenMaintenance(Long maintenanceId) {
        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new NotFoundException("Maintenance not found with id = " + maintenanceId));

        maintenance.setMaintenanceStatus(MaintenanceStatus.PENDING);
        maintenance.setUpdatedAt(Instant.now());
        maintenance.setCompletedAt(null);
        return mapToResponse(maintenanceRepository.save(maintenance));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new NotFoundException("Maintenance not found with id = " + id);
        }
        maintenanceRepository.deleteById(id);
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
