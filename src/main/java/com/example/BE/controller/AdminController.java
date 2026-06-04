package com.example.BE.controller;

import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.UserResponse;
import com.example.BE.enums.Role;
import com.example.BE.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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





}
