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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MaintenanceController {
    private final MaintenanceCommandService maintenanceCommandService;
    private final MaintenanceQueryService maintenanceQueryService;
    private final MaintenanceHistoryService maintenanceHistoryService;

    // Tạo maintenance - support both POST /api/maintenance and POST /api
    @PostMapping(value = {"/maintenance", ""})
    public ResponseEntity<MaintenanceTicketResponse> createMaintenance(
            @RequestBody CreateMaintenanceTicketRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(maintenanceCommandService.createMaintenance(request, authentication));
    }

    @PutMapping("/maintenance/{id}")
    public MaintenanceTicketResponse updateMaintenance(
            @PathVariable("id") Long maintenanceId,
            @RequestBody UpdateMaintenaceRequest request
    ) {
        return maintenanceCommandService.updateMaintenance(maintenanceId, request);
    }

    @PutMapping("/hotels/{hotelId}/maintenance/{id}")
    public MaintenanceTicketResponse updateMainternaceByHotelId(
            @PathVariable Long hotelId,
            @PathVariable("id") Long maintenanceId,
            @RequestBody UpdateMaintenaceRequest request
    ) {
        return maintenanceCommandService.updateMaintenanceByHotelId(hotelId, maintenanceId, request);
    }

    // Support PUT /api/{hotelId}/status?maintenainceId=123
    @PutMapping("/{hotelId}/status")
    public MaintenanceTicketResponse updateMaintenanceStatus(
            @PathVariable Long hotelId,
            @RequestParam(name = "maintenainceId", required = false) Long maintenainceId,
            @RequestBody UpdateMaintanceStatusRequest request
    ) {
        return maintenanceCommandService.updateMaintenanceStatus(hotelId, maintenainceId, request.getStatus());
    }

    // Direct endpoints PUT and PATCH /api/maintenance/{id}/status
    @PutMapping("/maintenance/{id}/status")
    public MaintenanceTicketResponse updateMaintenanceStatusDirectPut(
            @PathVariable("id") Long id,
            @RequestBody UpdateMaintanceStatusRequest request
    ) {
        return maintenanceCommandService.updateMaintenanceStatusDirect(id, request.getStatus());
    }

    @PatchMapping("/maintenance/{id}/status")
    public MaintenanceTicketResponse updateMaintenanceStatusDirectPatch(
            @PathVariable("id") Long id,
            @RequestBody UpdateMaintanceStatusRequest request
    ) {
        return maintenanceCommandService.updateMaintenanceStatusDirect(id, request.getStatus());
    }

    @PutMapping("/hotels/{hotelId}/maintenance/{id}/status")
    public MaintenanceTicketResponse updateHotelMaintenanceStatus(
            @PathVariable Long hotelId,
            @PathVariable("id") Long maintenanceId,
            @RequestBody UpdateMaintanceStatusRequest request
    ) {
        return maintenanceCommandService.updateMaintenanceStatus(hotelId, maintenanceId, request.getStatus());
    }

    @PutMapping("/maintenance/{hotelId}/reopen/{id}")
    public MaintenanceTicketResponse reopenMaintenance(
            @PathVariable("id") Long maintenanceId,
            @PathVariable Long hotelId
    ) {
        return maintenanceCommandService.reopenMaintenance(maintenanceId);
    }

    @GetMapping("/hotels/{hotelId}/maintenance")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMaintenanceByHotelId(@PathVariable Long hotelId) {
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByHotelId(hotelId));
    }

    @GetMapping("/maintenance")
    public ResponseEntity<List<MaintenanceTicketResponse>> getAllMaintenance() {
        return ResponseEntity.ok(maintenanceQueryService.getAllMaintenance());
    }

    @GetMapping("/maintenance/status")
    public ResponseEntity<List<MaintenanceTicketResponse>> getAllMaintenanceStatus(@RequestParam MaintenanceStatus status) {
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByStatus(status));
    }

    @GetMapping("/maintenance/equipment/{equipmentId}")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMaintenanceByEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByEquipment(equipmentId));
    }

    @GetMapping("/maintenance/room/{roomId}")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMaintenanceByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByRoom(roomId));
    }

    @GetMapping("/maintenance/user/{userId}")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMaintenanceByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(maintenanceQueryService.getMaintenanceByAssignedUser(userId));
    }
}
