package com.example.BE.dto.admin.request;

import com.example.BE.enums.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateEquipmentRequest {
    private Long id;
    private String name;
    private String brand;
    private String serialNumber;
    private EquipmentStatus status;
    private Long hotelId;
    private Long roomId;
    private String description;


}
