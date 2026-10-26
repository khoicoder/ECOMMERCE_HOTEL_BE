package com.example.BE.controller;

import com.example.BE.dto.admin.request.CreateEquipmentRequest;
import com.example.BE.dto.admin.request.UpdateEquipmentStatusRequest;
import com.example.BE.dto.admin.response.EquipmentResponse;
import com.example.BE.enums.EquipmentStatus;
import com.example.BE.services.Equipment.EquipmentService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentController {
    @Autowired
    private EquipmentService equipmentService;

    @PostMapping("/equipment")
    public ResponseEntity<EquipmentResponse> createEquipmentPost(@RequestBody CreateEquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.createEquipment(request));
    }

    @PatchMapping("/equipment")
    public ResponseEntity<EquipmentResponse> createEquipment(@RequestBody CreateEquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.createEquipment(request));
    }

    @GetMapping("/equipment/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @GetMapping("/hotels/{hotelId}/equipment")
    public ResponseEntity<List<EquipmentResponse>> getAllEquipmentByHotelId(@PathVariable Long hotelId) {
        return ResponseEntity.ok(equipmentService.getAllByHotelId(hotelId));
    }

    @PatchMapping("/equipment/{id}")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable Long id,
            @RequestBody UpdateEquipmentStatusRequest request
    ) {
        return ResponseEntity.ok(equipmentService.updateEquipmentStatus(id, request));
    }

    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<?> deleteEquipment(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.ok().build();
    }
}
