package com.example.BE.controller;

import com.example.BE.dto.admin.request.CreateMaintenanceTicketRequest;
import com.example.BE.dto.admin.request.UpdateMaintenaceRequest;
import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.services.Maintenance.MaintenanceCommandService;
import com.example.BE.services.Maintenance.MaintenanceHistoryService;
import com.example.BE.services.Maintenance.MaintenanceQueryService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels")
public class MaintenanceController {
    private final MaintenanceCommandService maintenanceCommandService;
    private final MaintenanceQueryService maintenanceQueryService;
    private final MaintenanceHistoryService maintenanceHistoryService;
    //Tạo maintenance
    @PostMapping
    public MaintenanceTicketResponse createMaintenance(@RequestBody CreateMaintenanceTicketRequest request, Authentication authentication){
        return maintenanceCommandService.createMaintenance(request,authentication);
    }
    @PutMapping("/maintenance/{id}")
    public MaintenanceTicketResponse updateMaintenance(@PathVariable Long maintenanceId,
                                                             @RequestBody UpdateMaintenaceRequest request){
        return maintenanceCommandService.updateMaintenance(maintenanceId,request);

    }
    @PutMapping("/{hotelId}//maintenance/{id}")
    public MaintenanceTicketResponse updateMainternaceByHotelId(@PathVariable Long hotelId, @PathVariable Long maintenanceId,@RequestBody UpdateMaintenaceRequest request){
        return maintenanceCommandService.updateMaintenanceByHotelId(hotelId,maintenanceId,request);
    }

    //querry




}
