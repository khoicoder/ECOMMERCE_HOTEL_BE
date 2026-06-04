package com.example.BE.services;


import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.UserResponse;
import com.example.BE.enums.Role;
import org.springframework.data.domain.Page;

public interface AdminService {
    Page<UserResponse> getAllUsers(String keyword,String role,boolean active,int page, int size);
    UserResponse getUserById(Long id);
    UserResponse changeRole(Long id, ChangeRoleRequest request);
}
