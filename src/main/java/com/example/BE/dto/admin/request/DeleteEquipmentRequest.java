package com.example.BE.dto.admin.request;

import com.example.BE.enums.Role;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteEquipmentRequest {
    private Long id;
    private String nameItem;
    private Integer quantity;
    private Role role;
    private Long userId;
    private Instant createdAt;
}
