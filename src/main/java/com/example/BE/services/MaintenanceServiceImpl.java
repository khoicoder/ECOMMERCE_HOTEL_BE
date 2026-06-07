package com.example.BE.services;

import com.example.BE.dto.admin.request.CreateMaintenanceTicketRequest;
import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.EquipmentStatus;
import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.model.EquipmentModel;
import com.example.BE.model.HotelModel;
import com.example.BE.model.MaintenanceModel;
import com.example.BE.model.UserModel;
import com.example.BE.repository.EquipmentRepository;
import com.example.BE.repository.HotelRepository;
import com.example.BE.repository.MaintenanceRepository;
import com.example.BE.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@NoArgsConstructor
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {
    private MaintenanceRepository maintenanceRepository;
    private HotelRepository hotelRepository;
    private UserRepository userRepository;
    private EquipmentRepository equipmentRepository;


    @Override
    public List<MaintenanceTicketResponse> getMaintenanceByHotelId(Long hotelId) {
        List<MaintenanceModel> maintenance = maintenanceRepository.findByHotelId(hotelId);
        if(maintenance.isEmpty()){
            throw new NotFoundException("Maintenance not found hotelId = "+hotelId);
        }
        return maintenance.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MaintenanceTicketResponse createMaintenance(CreateMaintenanceTicketRequest request) {
        HotelModel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new NotFoundException("Hotel not found with id = " + request.getHotelId()));
        UserModel creator =  userRepository.findById(request.getCreatedById()).orElseThrow(
                () -> new NotFoundException("User not found with id = " + request.getCreatedById())
        );
        MaintenanceModel maintenance = new MaintenanceModel();

        maintenance.setHotel(hotel);
        maintenance.setCreatedAt(LocalDateTime.now());
        maintenance.setDescription(request.getDescription());
        maintenance.setTitle(request.getTitle());

        maintenance.setCreatedBy(creator);


        return mapToResponse(maintenanceRepository.save(maintenance));
    }

    @Override
    public List<MaintenanceTicketResponse> getAllMaintenance() {
        return List.of();
    }

    @Override
    public List<MaintenanceTicketResponse> getByHotelId(Long hotelId) {
        return List.of();
    }

    @Override

    @Transactional
    public MaintenanceTicketResponse updateMaintenanceStatus(Long hotelId,Long maintenaceId, MaintenanceStatus status) {
        HotelModel hotel = hotelRepository.findById(hotelId).orElseThrow(()
                -> new NotFoundException("Hotel not found with id = " + hotelId));
        MaintenanceModel maintenance = maintenanceRepository.findById(maintenaceId).orElseThrow(()
        -> new NotFoundException("Maintenance not found with id = " + maintenaceId));
        if(!maintenance.getHotel().equals(hotel.getId())){
            throw new NotFoundException("Hotel Id and Hotel Id doesn't match"+hotelId);

        };
        maintenance.setMaintenanceStatus(status);
        maintenance.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(maintenanceRepository.save(maintenance));

    }

    @Override
    public void delete(Long id) {

    }
    public MaintenanceTicketResponse mapToResponse(MaintenanceModel maintenance) {
        return new MaintenanceTicketResponse(
                maintenance.getId(),
                maintenance.getHotel().getId(),
                maintenance.getRoom().getId(),
                maintenance.getEquipment().getEquipmentId(),
                maintenance.getTitle(),
                maintenance.getDescription(),
                maintenance.getMaintenanceStatus(),
                maintenance.getMaintenanceType(),
                maintenance.getCreatedBy().getCreatedBy(),
                maintenance.getCreatedAt(),
                maintenance.getUpdatedAt(),
                maintenance.getCreatedBy() != null ? maintenance.getCreatedBy().getUsername() : null,
                maintenance.getCreatedBy() != null ? maintenance.getCreatedBy().getRole().name() : null





        );
    }
}
