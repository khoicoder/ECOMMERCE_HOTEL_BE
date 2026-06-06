package com.example.BE.services;

import com.example.BE.dto.admin.request.CreateEquipmentRequest;
import com.example.BE.dto.admin.response.EquipmentResponse;
import com.example.BE.enums.EquipmentStatus;

import java.util.List;

public interface EquipmentService {
    EquipmentResponse create(CreateEquipmentRequest request);
    EquipmentResponse getEquipmentById(Long id);
    List<EquipmentResponse> getAllEquipments();
    List<EquipmentResponse> getByHotelId(Long hotelId);
    EquipmentResponse updateEquipmentStatus(Long id, EquipmentStatus status);


}
