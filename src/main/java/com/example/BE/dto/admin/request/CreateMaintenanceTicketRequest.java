package com.example.BE.dto.admin.request;

import com.example.BE.model.EquipmentModel;
import com.example.BE.model.UserModel;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaintenanceTicketRequest {
    private Long createdById;
    private Long hotelId;
    private Long roomId;
    private Long equipmentId;
    private String title;

    private String description;
}
