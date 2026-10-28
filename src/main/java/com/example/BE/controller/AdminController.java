package com.example.BE.controller;

import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.DashboardResponse;
import com.example.BE.dto.admin.response.SessionResponse;
import com.example.BE.dto.admin.response.UserResponse;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.model.HotelModel;
import com.example.BE.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(keyword, role, active, page, size, authentication));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> changeRole(
            @PathVariable Long id,
            @RequestBody ChangeRoleRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.changeRole(id, request, authentication));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(adminService.getDashboard(authentication));
    }

    @GetMapping("/dashboard-executive")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboardExecutive(Authentication authentication) {
        return ResponseEntity.ok(adminService.getDashboard(authentication));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAdminBookings(Authentication authentication) {
        return ResponseEntity.ok(adminService.getAdminBookings(authentication));
    }

    @GetMapping("/hotels")
    public ResponseEntity<List<HotelModel>> getAdminHotels(Authentication authentication) {
        return ResponseEntity.ok(adminService.getAdminHotels(authentication));
    }

    @PatchMapping("/users/{id}/lock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> lockUser(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(adminService.lockUser(id, authentication));
    }

    @PatchMapping("/users/{id}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> unlockUser(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(adminService.unlockUser(id, authentication));
    }

    @GetMapping("/users/{id}/all-sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<SessionResponse>> getAllSession(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAllSessionById(id));
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SessionResponse> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(adminService.getSessionById(sessionId));
    }

    // =========================================================================
    // QUẢN LÝ QUẢN TRỊ VIÊN KHÁCH SẠN (HOTEL ADMINS) - DÀNH CHO SUPER_ADMIN
    // =========================================================================

    @GetMapping("/hotel-admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<com.example.BE.dto.admin.response.HotelAdminResponse>> getHotelAdmins(
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.getHotelAdmins(authentication));
    }

    @PostMapping("/hotel-admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.example.BE.dto.admin.response.HotelAdminResponse> createHotelAdmin(
            @jakarta.validation.Valid @RequestBody com.example.BE.dto.admin.request.CreateHotelAdminRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.createHotelAdmin(request, authentication));
    }

    @PutMapping("/hotel-admins/{hotelId}/assign/{adminUserId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.example.BE.dto.admin.response.HotelAdminResponse> assignHotelAdmin(
            @PathVariable Long hotelId,
            @PathVariable Long adminUserId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.assignHotelAdmin(hotelId, adminUserId, authentication));
    }

    // =========================================================================
    // QUẢN LÝ LỄ TÂN (RECEPTIONISTS) - DÀNH CHO ADMIN MỖI KHÁCH SẠN & SUPER_ADMIN
    // =========================================================================

    @GetMapping("/receptionists")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<com.example.BE.dto.admin.response.ReceptionistResponse>> getReceptionists(
            @RequestParam(required = false) Long hotelId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.getReceptionists(hotelId, authentication));
    }

    @PostMapping("/receptionists")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<com.example.BE.dto.admin.response.ReceptionistResponse> createReceptionist(
            @jakarta.validation.Valid @RequestBody com.example.BE.dto.admin.request.CreateReceptionistRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.createReceptionist(request, authentication));
    }

    @PatchMapping("/receptionists/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<com.example.BE.dto.admin.response.ReceptionistResponse> toggleReceptionistStatus(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.toggleReceptionistStatus(id, authentication));
    }

    @PostMapping("/receptionists/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> resetReceptionistPassword(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody com.example.BE.dto.admin.request.ResetStaffPasswordRequest request,
            Authentication authentication
    ) {
        adminService.resetReceptionistPassword(id, request, authentication);
        return ResponseEntity.ok(java.util.Map.of("message", "Đổi mật khẩu thành công"));
    }
}
