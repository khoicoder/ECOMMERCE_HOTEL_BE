package com.example.BE.dto.admin.request;



public record UpdateMaintenaceRequest(
        String title,
        String description,
        Long assignedToId,
        Long roomId,
        Long equipmentId
) {



}
