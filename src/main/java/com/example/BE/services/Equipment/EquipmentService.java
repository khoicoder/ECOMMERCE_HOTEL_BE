package com.example.BE.services.Equipment;

import com.example.BE.dto.admin.request.CreateEquipmentRequest;
import com.example.BE.dto.admin.request.DeleteEquipmentRequest;
import com.example.BE.dto.admin.request.UpdateEquipmentStatusRequest;
import com.example.BE.dto.admin.response.EquipmentResponse;
import com.example.BE.enums.EquipmentStatus;

import java.util.List;

public interface EquipmentService {
    EquipmentResponse createEquipment(CreateEquipmentRequest request);
    EquipmentResponse deleteEquiment(DeleteEquipmentRequest request);
    EquipmentResponse getEquipmentById(Long id);
    List<EquipmentResponse> getAllEquipmentWorkingInHotel(EquipmentStatus statusWorking, Long hotelId);
    List<EquipmentResponse> getAllByHotelId(Long hotelId);
    EquipmentResponse updateEquipmentStatus(Long id, UpdateEquipmentStatusRequest requestStatus);
    void delete(Long id);


}
