package com.example.BE.controller;

import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.DashboardResponse;
import com.example.BE.dto.admin.response.SessionResponse;
import com.example.BE.dto.admin.response.UserResponse;
import com.example.BE.enums.Role;
import com.example.BE.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) boolean active,
            @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size
        ) {
        return
          ResponseEntity.ok(adminService.getAllUsers(keyword,role,active,page,size));
    }
    @GetMapping("users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id){
        return ResponseEntity.ok(adminService.getUserById(id));
    }
    @PatchMapping("users/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id,@RequestBody ChangeRoleRequest request){
        return ResponseEntity.ok(adminService.changeRole(id,request));

    }
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(){
        return ResponseEntity.ok(adminService.getDashboard());
    }
    @PatchMapping("/users/{id}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Long id){
        return ResponseEntity.ok(adminService.lockUser(id));
    }
    @PatchMapping("/users/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long id){
        return ResponseEntity.ok(adminService.unlockUser(id));
    }
    @GetMapping("/users/{id}/all-sessions")
    public ResponseEntity<List<SessionResponse>> getAllSession(@PathVariable Long id){
        return ResponseEntity.ok(adminService.getAllSessionById(id));
    }
    @GetMapping("sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable UUID sessionId){
        return ResponseEntity.ok(adminService.getSessionById(sessionId));
    }




}
