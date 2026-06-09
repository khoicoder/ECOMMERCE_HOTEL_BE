package com.example.BE.controller;

import com.example.BE.dto.admin.request.CreateMaintenanceTicketRequest;
import com.example.BE.dto.admin.request.UpdateMaintanceStatusRequest;
import com.example.BE.dto.admin.request.UpdateMaintenaceRequest;
import com.example.BE.dto.admin.response.MaintenanceTicketResponse;
import com.example.BE.enums.MaintenanceStatus;
import com.example.BE.services.Maintenance.MaintenanceCommandService;
import com.example.BE.services.Maintenance.MaintenanceHistoryService;
import com.example.BE.services.Maintenance.MaintenanceQueryService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MaintenanceController {
    private final MaintenanceCommandService maintenanceCommandService;
    private final MaintenanceQueryService maintenanceQueryService;
    private final MaintenanceHistoryService maintenanceHistoryService;
    //Tạo maintenance
    @PostMapping
    public ResponseEntity<MaintenanceTicketResponse> createMaintenance(@RequestBody CreateMaintenanceTicketRequest request, Authentication authentication){
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceCommandService.createMaintenance(request,authentication));
    }
    @PutMapping("/maintenance/{id}")
    public MaintenanceTicketResponse updateMaintenance(@PathVariable Long maintenanceId,
                                                             @RequestBody UpdateMaintenaceRequest request){
        return maintenanceCommandService.updateMaintenance(maintenanceId,request);

    }
    @PutMapping("/hotels/{hotelId}/maintenance/{id}")
    public MaintenanceTicketResponse updateMainternaceByHotelId(@PathVariable Long hotelId, @PathVariable Long maintenanceId,@RequestBody UpdateMaintenaceRequest request){
        return maintenanceCommandService.updateMaintenanceByHotelId(hotelId,maintenanceId,request);
    }
    @PutMapping("/{hotelId}/status")
    public MaintenanceTicketResponse updateMaintenanceStatus(@PathVariable Long hotelId,@PathVariable Long maintenainceId,@RequestBody UpdateMaintanceStatusRequest request){
        return maintenanceCommandService.updateMaintenanceStatus(hotelId,maintenainceId,request.getStatus());
    }
    @PutMapping("/maintenance/{hotelId}/maintenance/{id}")
    public MaintenanceTicketResponse reopenMaintenance(@PathVariable Long maintenanceId,@PathVariable Long hotelId){

        return null;
    }
    @GetMapping("/hotels/{hotelId}/maintenance")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMaintenanceByHotelId(@PathVariable Long hotelId){
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByHotelId(hotelId));
    }
    @GetMapping("/maintenance")
    public ResponseEntity<List<MaintenanceTicketResponse>> getAllMaintenance(){
        return ResponseEntity.ok(maintenanceQueryService.getAllMaintenance());
    }
    @GetMapping("/maintenance/status")
    public ResponseEntity<List<MaintenanceTicketResponse>> getAllMaintenanceStatus(@PathVariable MaintenanceStatus status){
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByStatus(status));
    }
    @GetMapping("/maintenance/equipment/{equipmentId}")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMaintenanceByEquipment(@PathVariable Long equipmentId){
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByEquipment(equipmentId));
    }
    @GetMapping("/maintenance/room/{roomId}")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMaintenanceByRoom(@PathVariable Long roomId){
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByRoom(roomId));
    }
    @GetMapping("/maintenance/user/{userId}")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMaintenanceByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByAssignedUser(userId));
    }









}
