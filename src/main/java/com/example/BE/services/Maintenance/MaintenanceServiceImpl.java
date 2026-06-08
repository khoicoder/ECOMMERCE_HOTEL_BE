//package com.example.BE.services.Maintenance;
//
//import com.example.BE.dto.admin.request.CreateMaintenanceTicketRequest;
//import com.example.BE.dto.admin.response.MaintenanceHistoryResponse;
//import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
//import com.example.BE.enums.MaintenanceStatus;
//import com.example.BE.exception.BadRequestException;
//import com.example.BE.exception.NotFoundException;
//import com.example.BE.model.HotelModel;
//import com.example.BE.model.MaintenanceModel;
//import com.example.BE.model.UserModel;
//import com.example.BE.repository.EquipmentRepository;
//import com.example.BE.repository.HotelRepository;
//import com.example.BE.repository.MaintenanceRepository;
//import com.example.BE.repository.UserRepository;
//import lombok.NoArgsConstructor;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@NoArgsConstructor
//@RequiredArgsConstructor
//public class MaintenanceServiceImpl implements MaintenanceService {
//    private MaintenanceRepository maintenanceRepository;
//    private HotelRepository hotelRepository;
//    private UserRepository userRepository;
//    private EquipmentRepository equipmentRepository;
//
//
//    @Override
//    public List<MaintenanceTicketResponse> getMaintenanceByHotelId(Long hotelId) {
//        List<MaintenanceModel> maintenance = maintenanceRepository.findByHotelId(hotelId);
//        if(maintenance.isEmpty()){
//            throw new NotFoundException("Maintenance not found hotelId = "+hotelId);
//        }
//        return maintenance.stream()
//                .map(this::mapToResponse)
//                .toList();
//    }
//
//    @Override
//    public MaintenanceTicketResponse createMaintenance(CreateMaintenanceTicketRequest request) {
//        HotelModel hotel = hotelRepository.findById(request.getHotelId())
//                .orElseThrow(() -> new NotFoundException("Hotel not found with id = " + request.getHotelId()));
//        UserModel creator =  userRepository.findById(request.getCreatedById()).orElseThrow(
//                () -> new NotFoundException("User not found with id = " + request.getCreatedById())
//        );
//        MaintenanceModel maintenance = new MaintenanceModel();
//
//        maintenance.setHotel(hotel);
//        maintenance.setCreatedAt(LocalDateTime.now());
//        maintenance.setDescription(request.getDescription());
//        maintenance.setTitle(request.getTitle());
//
//        maintenance.setCreatedBy(creator);
//
//
//        return mapToResponse(maintenanceRepository.save(maintenance));
//    }
//
//    @Override
//    public List<MaintenanceTicketResponse> getAllMaintenance() {
//        return List.of();
//    }
//
//    @Override
//    public int hashCode() {
//        return super.hashCode();
//    }
//
//    @Override
//    public List<MaintenanceTicketResponse> getMaintenanceByEquipment(Long equipmentId) {
//        return List.of();
//    }
//
//    @Override
//    public List<MaintenanceTicketResponse> getMaintenanceByRoom() {
//        return List.of();
//    }
//
//    @Override
//    public List<MaintenanceTicketResponse> getMaintenanceByAssignedUser(Long userId) {
//        return List.of();
//    }
//
//    @Override
//    public MaintenanceTicketResponse reopenMaintenance(Long maintenanceId) {
//        return null;
//    }
//
//    @Override
//    public List<MaintenanceHistoryResponse> getMaintenanceHistory(Long maintenanceId) {
//        return List.of();
//    }
//
//    @Override
//    @Transactional
//    public MaintenanceTicketResponse updateMaintenanceStatus(Long hotelId,Long maintenaceId, MaintenanceStatus newStatus) {
//        HotelModel hotel = hotelRepository.findById(hotelId).orElseThrow(()
//                -> new NotFoundException("Hotel not found with id = " + hotelId));
//        MaintenanceModel maintenance = maintenanceRepository.findById(maintenaceId).orElseThrow(()
//        -> new NotFoundException("Maintenance not found with id = " + maintenaceId));
//        if(!maintenance.getHotel().equals(hotel.getId())){
//            throw new NotFoundException("Hotel Id and Hotel Id doesn't match"+hotelId);
//
//        };
//        if(maintenance.getHotel() ==null || !maintenance.getHotel().getId().equals(hotel.getId())){
//            throw new BadRequestException("Hotel in maintenance :"+maintenaceId+"doesn't match hotel+ "+hotelId);
//
//        }
//        MaintenanceStatus currentStatus = maintenance.getMaintenanceStatus();
//        if(!isValidMaintenanceTransition(currentStatus, newStatus)){
//            throw new BadRequestException("Invalid Maintenance Status transition from "+currentStatus+" to "+newStatus);
//        }
//        maintenance.setMaintenanceStatus(newStatus);
//        maintenance.setUpdatedAt(LocalDateTime.now());
//        if(newStatus == MaintenanceStatus.COMPLETED){
//            maintenance.setCompletedAt(LocalDateTime.now());
//        }
//        MaintenanceModel save = maintenanceRepository.save(maintenance);
//        return mapToResponse(save);
//
//    }
//    @Override
//    public List<MaintenanceTicketResponse> getMaintenanceByStatus(MaintenanceStatus maintenanceStatus) {
//    return null;
//
//    }
//
//    @Override
//    public void delete(Long id) {
//
//    }
//
//    private boolean isValidMaintenanceTransition(MaintenanceStatus currentStatus, MaintenanceStatus nextStatus) {
//        if(currentStatus == null){
//            return nextStatus == MaintenanceStatus.PENDING;
//        }
//        return switch (currentStatus) {
//            case PENDING ->
//                nextStatus == MaintenanceStatus.IN_PROGRESS
//                        || nextStatus == MaintenanceStatus.CANCELLED;
//            case IN_PROGRESS ->
//                nextStatus == MaintenanceStatus.COMPLETED
//                        || nextStatus == MaintenanceStatus.CANCELLED ;
//            case COMPLETED ,CANCELLED -> false;
//        };
//    }
//    public MaintenanceTicketResponse mapToResponse(MaintenanceModel maintenance) {
//        return new MaintenanceTicketResponse(
//                maintenance.getId(),
//                maintenance.getHotel() != null
//                        ? maintenance.getHotel().getId()
//                        : null,
//                maintenance.getRoom()!=null
//                        ?maintenance.getRoom().getId()
//                        : null,
//                maintenance.getEquipment()!=null
//                        ? maintenance.getEquipment().getEquipmentId()
//                        : null,
//                maintenance.getTitle(),
//                maintenance.getDescription(),
//                maintenance.getMaintenanceStatus(),
//                maintenance.getMaintenanceType(),
//                maintenance.getCreatedBy()!= null
//                        ?maintenance.getCreatedBy().getCreatedBy()
//                        : null,
//                maintenance.getCreatedAt(),
//                maintenance.getUpdatedAt(),
//                maintenance.getCreatedBy() != null
//                        ? maintenance.getCreatedBy().getUsername()
//                        : null,
//                maintenance.getCreatedBy() != null && maintenance.getCreatedBy().getRole()!=null
//                        ? maintenance.getCreatedBy().getRole().name()
//                        : null
//
//
//        );
//    }
//}
