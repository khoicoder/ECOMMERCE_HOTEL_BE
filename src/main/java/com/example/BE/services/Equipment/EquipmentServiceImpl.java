package com.example.BE.services.Equipment;

import com.example.BE.dto.admin.request.CreateEquipmentRequest;
import com.example.BE.dto.admin.request.UpdateEquipmentStatusRequest;
import com.example.BE.dto.admin.response.EquipmentResponse;
import com.example.BE.enums.EquipmentStatus;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.model.EquipmentModel;
import com.example.BE.model.HotelModel;
import com.example.BE.model.RoomModel;
import com.example.BE.repository.EquipmentRepository;
import com.example.BE.repository.HotelRepository;
import com.example.BE.repository.RoomRepository;
import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Service
@AllArgsConstructor
@NoArgsConstructor

public class EquipmentServiceImpl implements EquipmentService {
    private  EquipmentRepository equipmentRepository;
    private HotelRepository hotelRepository;
    private  RoomRepository roomRepository;


        @Override
        @Transactional
        public EquipmentResponse createEquipment(CreateEquipmentRequest request) {
            HotelModel hotel = hotelRepository.findById(request.getHotelId()).orElseThrow(()->
                    new NotFoundException("Hotel Not Found"));
            EquipmentModel equipment = new EquipmentModel();
            equipment.setName(request.getName());
            equipment.setBrand(request.getBrand());
            equipment.setSerialNumber(request.getSerialNumber());
            equipment.setDescription(request.getDescription());
            equipment.setHotel(hotel);
            equipment.setStatus(EquipmentStatus.WORKING);
            equipment.setCreatedAt(LocalDateTime.now());
            equipment.setUpdatedAt(LocalDateTime.now());
            if(request.getRoomId() != null) {
                RoomModel room = roomRepository.findById(request.getRoomId()).orElseThrow(()
                -> new NotFoundException("Room Not Found"));
                equipment.setRoom(room);

            }

            return mapToEquipmentResponse( equipmentRepository.save(equipment)
            );

    }

    @Override
    public EquipmentResponse getEquipmentById(Long id) {
            EquipmentModel equiment = equipmentRepository.findById(id).orElseThrow(()->
                    new  NotFoundException("Equipment Not Found"+ id));

        return mapToEquipmentResponse(equiment);
    }



    @Override
    public void delete(Long id) {

    }

    @Override
    public List<EquipmentResponse> getAllEquipmentWorkingInHotel(EquipmentStatus statusWorking,Long hotelId) {
            HotelModel hotel = hotelRepository.findById(hotelId).orElseThrow(()
                    ->new  NotFoundException("Hotel Not Found"+ hotelId));
            List<EquipmentModel> equipment = equipmentRepository.findByHotel(hotelId);

            if(equipment.isEmpty()){
                throw new BadRequestException("Invalid Status");
            }

        return equipment.stream().map(this::mapToEquipmentResponse).toList();
    }

    @Override
    public List<EquipmentResponse> getAllByHotelId(Long hotelId) {
            hotelRepository.findById(hotelId).orElseThrow(()->
                    new NotFoundException("Hotel Not Found"+ hotelId));
            List<EquipmentModel> equipment = equipmentRepository.findByHotel(hotelId);
            if (equipment.isEmpty()) {
                throw new NotFoundException("Equipment Not Found In Hotel Id"+hotelId);
            }


        return equipment.stream().map(this::mapToEquipmentResponse).toList();
    }

    @Override
    public EquipmentResponse updateEquipmentStatus(Long id, UpdateEquipmentStatusRequest requestStatus) {
            EquipmentModel equipment = equipmentRepository.findById(id).orElseThrow(()
                    -> new   NotFoundException("Equipment Not Found"+ id));
            equipment.setStatus(requestStatus.getStatus());
            equipment.setUpdatedAt(LocalDateTime.now());


        return mapToEquipmentResponse(equipmentRepository.save(equipment));
    }
    private EquipmentResponse mapToEquipmentResponse(EquipmentModel equipment) {
        return new  EquipmentResponse(
                equipment.getEquipmentId(),
                equipment.getName(),
                equipment.getBrand(),
                equipment.getSerialNumber(),
                equipment.getStatus(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt(),
                equipment.getHotel().getId(),
                equipment.getRoom() != null
                    ? equipment.getRoom().getId(): null,
                equipment.getDescription()
        );
    }
}
