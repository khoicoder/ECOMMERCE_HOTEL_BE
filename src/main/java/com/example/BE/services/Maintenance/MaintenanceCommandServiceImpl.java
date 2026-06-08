package com.example.BE.services.Maintenance;

import com.example.BE.dto.admin.request.CreateMaintenanceTicketRequest;
import com.example.BE.dto.admin.request.UpdateMaintenaceRequest;
import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.MaintenanceStatus;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MaintenanceCommandServiceImpl implements MaintenanceCommandService {

    private final MaintenanceRepository maintenanceRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    @Override
    public MaintenanceTicketResponse updateMaintenanceByHotelId(Long hotelId,Long maintenanceId,UpdateMaintenaceRequest request) {
        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(()->
                new NotFoundException("Maintenance with id: " + maintenanceId));

        if(maintenance.getHotel() == null || !maintenance.getHotel().getId().equals(hotelId)){
            throw new BadRequestException("Maintenance doesn't belong to this hotel"+hotelId);

        }

        if(request.title() != null || request.title().isBlank()) {
            maintenance.setTitle(request.title());
        }
        if(request.description() != null || request.description().isBlank()) {
            maintenance.setDescription(request.description());
        }
        if(request.assignedToId()!=null){
            UserModel assignedUser = userRepository.findById(request.assignedToId()).orElseThrow(()
                    -> new  NotFoundException("Equipment with id: " + request.equipmentId()));
            maintenance.setAssignedTo(assignedUser);
        }
        if(request.roomId()!=null){
            RoomModel room = roomRepository.findById(request.roomId()).orElseThrow(()
                    -> new NotFoundException("Room not found with id :"+request.roomId()));
            maintenance.setRoom(room);
        }
        maintenance.setUpdatedAt(LocalDateTime.now());
        MaintenanceModel save = maintenanceRepository.save(maintenance);


        return mapToResponse(save);
    }

    @Override
    public MaintenanceTicketResponse updateMaintenance(Long maintenanceId,UpdateMaintenaceRequest request) {
        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(()->
                new NotFoundException("Maintenance with id: " + maintenanceId));


        MaintenanceModel maintenace = new MaintenanceModel();
        if(request.title() != null || request.title().isBlank()) {
            maintenance.setTitle(request.title());
        }
        if(request.description() != null || request.description().isBlank()) {
            maintenance.setDescription(request.description());
        }
        if(request.assignedToId()!=null){
            UserModel assignedUser = userRepository.findById(request.assignedToId()).orElseThrow(()
                    -> new  NotFoundException("Equipment with id: " + request.equipmentId()));
            maintenance.setAssignedTo(assignedUser);
        }
        if(request.roomId()!=null){
            RoomModel room = roomRepository.findById(request.roomId()).orElseThrow(()
                    -> new NotFoundException("Room not found with id :"+request.roomId()));
            maintenance.setRoom(room);
        }
        maintenance.setUpdatedAt(LocalDateTime.now());
        MaintenanceModel save = maintenanceRepository.save(maintenance);


        return mapToResponse(save);
    }

    @Override
    @Transactional
    public MaintenanceTicketResponse createMaintenance(CreateMaintenanceTicketRequest request, Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        HotelModel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new NotFoundException("Hotel not found with id = " + request.getHotelId()));

        UserModel creator = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new NotFoundException("User not found with id = " + request.getCreatedById()));

        MaintenanceModel maintenance = new MaintenanceModel();
        maintenance.setHotel(hotel);
        maintenance.setCreatedBy(creator);
        maintenance.setCreatedAt(LocalDateTime.now());
        maintenance.setUpdatedAt(LocalDateTime.now());
        maintenance.setTitle(request.getTitle());
        maintenance.setDescription(request.getDescription());
        maintenance.setMaintenanceStatus(MaintenanceStatus.PENDING);

        if (request.getRoomId() != null) {
            RoomModel room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new NotFoundException("Room not found with id = " + request.getRoomId()));
            maintenance.setRoom(room);
        }

        if (request.getEquipmentId() != null) {
            EquipmentModel equipment = equipmentRepository.findById(request.getEquipmentId())
                    .orElseThrow(() -> new NotFoundException("Equipment not found with id = " + request.getEquipmentId()));
            maintenance.setEquipment(equipment);
        }

        return mapToResponse(maintenanceRepository.save(maintenance));
    }

    @Override
    @Transactional
    public MaintenanceTicketResponse updateMaintenanceStatus(Long hotelId, Long maintenanceId, MaintenanceStatus newStatus) {
        HotelModel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id = " + hotelId));

        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new NotFoundException("Maintenance not found with id = " + maintenanceId));

        if (maintenance.getHotel() == null || !maintenance.getHotel().getId().equals(hotel.getId())) {
            throw new BadRequestException(
                    "Maintenance " + maintenanceId + " does not belong to hotel " + hotelId
            );
        }

        MaintenanceStatus currentStatus = maintenance.getMaintenanceStatus();
        if (!isValidMaintenanceTransition(currentStatus, newStatus)) {
            throw new BadRequestException(
                    "Invalid maintenance status transition from " + currentStatus + " to " + newStatus
            );
        }

        maintenance.setMaintenanceStatus(newStatus);
        maintenance.setUpdatedAt(LocalDateTime.now());

        if (newStatus == MaintenanceStatus.COMPLETED) {
            maintenance.setCompletedAt(LocalDateTime.now());
        }

        return mapToResponse(maintenanceRepository.save(maintenance));
    }

    @Override
    @Transactional
    public MaintenanceTicketResponse reopenMaintenance(Long maintenanceId) {
        MaintenanceModel maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new NotFoundException("Maintenance not found with id = " + maintenanceId));

        if (maintenance.getMaintenanceStatus() != MaintenanceStatus.COMPLETED
                && maintenance.getMaintenanceStatus() != MaintenanceStatus.CANCELLED) {
            throw new BadRequestException("Only COMPLETED or CANCELLED maintenance can be reopened");
        }

        maintenance.setMaintenanceStatus(MaintenanceStatus.PENDING);
        maintenance.setUpdatedAt(LocalDateTime.now());
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

    private boolean isValidMaintenanceTransition(MaintenanceStatus currentStatus, MaintenanceStatus nextStatus) {
        if (currentStatus == null) {
            return nextStatus == MaintenanceStatus.PENDING;
        }

        return switch (currentStatus) {
            case PENDING -> nextStatus == MaintenanceStatus.IN_PROGRESS
                    || nextStatus == MaintenanceStatus.CANCELLED;
            case IN_PROGRESS -> nextStatus == MaintenanceStatus.COMPLETED
                    || nextStatus == MaintenanceStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
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