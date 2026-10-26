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
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
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
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PatchMapping("/users/{id}/role")
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
    public ResponseEntity<?> lockUser(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(adminService.lockUser(id, authentication));
    }

    @PatchMapping("/users/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(adminService.unlockUser(id, authentication));
    }

    @GetMapping("/users/{id}/all-sessions")
    public ResponseEntity<List<SessionResponse>> getAllSession(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAllSessionById(id));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(adminService.getSessionById(sessionId));
    }
}
