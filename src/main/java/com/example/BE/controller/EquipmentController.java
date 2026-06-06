package com.example.BE.controller;

import com.example.BE.model.EquipmentModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipments")
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentController {
    @PostMapping
    public ResponseEntity<EquipmentModel> createEquipment(@RequestBody EquipmentModel equipment) {
        return ResponseEntity.ok(equipment);
    }



}
