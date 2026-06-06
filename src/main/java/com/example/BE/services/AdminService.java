package com.example.BE.services;


import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.DashboardResponse;
import com.example.BE.dto.admin.response.SessionResponse;
import com.example.BE.dto.admin.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    Page<UserResponse> getAllUsers(String keyword,String role,boolean active,int page, int size);
    UserResponse getUserById(Long id);
    UserResponse changeRole(Long id, ChangeRoleRequest request);
    DashboardResponse getDashboard();
    UserResponse lockUser(Long id);
    UserResponse unlockUser(Long id);
    List<SessionResponse> getAllSessionById(Long id);
    SessionResponse getSessionById(UUID sessionId);



}
