package com.example.BE.dto.admin.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaintenanceTicketRequest {
    private Long hotelId;
    private Long roomId;
    private Long equipmentId;
    private String title;
    private String description;
}
