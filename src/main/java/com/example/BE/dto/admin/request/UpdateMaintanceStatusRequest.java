package com.example.BE.dto.admin.request;

import com.example.BE.enums.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMaintanceStatusRequest {
    private MaintenanceStatus status;
}
