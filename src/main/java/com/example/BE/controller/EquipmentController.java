package com.example.BE.controller;

import com.example.BE.dto.admin.request.CreateEquipmentRequest;
import com.example.BE.dto.admin.request.UpdateEquipmentStatusRequest;
import com.example.BE.dto.admin.response.EquipmentResponse;
import com.example.BE.enums.EquipmentStatus;
import com.example.BE.model.EquipmentModel;
import com.example.BE.services.Equipment.EquipmentService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentController {
    private EquipmentService equipmentService;
    @PatchMapping("/equipment")
    public ResponseEntity<EquipmentResponse> createEquipment(@RequestBody CreateEquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.createEquipment(request));
    }
    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@RequestBody  Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }
    @GetMapping("/hotels/{hotelId}/equipment/{equipmentId}")
    public ResponseEntity<List<EquipmentResponse>>  getAllEquipmentWorkingInHotel(@PathVariable EquipmentStatus status, @PathVariable Long hotelId){
        return ResponseEntity.ok(equipmentService.getAllEquipmentWorkingInHotel(status,hotelId));
    }
    @GetMapping("/hotels/{hotelId}/equipment")
    public ResponseEntity<List<EquipmentResponse>> getAllEquipmentByHotelId(@PathVariable Long hotelId){
        return ResponseEntity.ok(equipmentService.getAllByHotelId(hotelId));
    }
    @PatchMapping("/equipment/{id}")
    public ResponseEntity<EquipmentResponse> updateEquipment(@PathVariable Long id,@RequestBody UpdateEquipmentStatusRequest request) {
        return ResponseEntity.ok(equipmentService.updateEquipmentStatus(id, request));
    }




}
