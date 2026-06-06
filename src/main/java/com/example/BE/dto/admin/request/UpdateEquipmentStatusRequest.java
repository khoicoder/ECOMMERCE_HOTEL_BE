package com.example.BE.dto.admin.request;

import com.example.BE.enums.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEquipmentStatusRequest {
    private EquipmentStatus status;
}
