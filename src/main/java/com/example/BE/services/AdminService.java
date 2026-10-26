package com.example.BE.services;

import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.DashboardResponse;
import com.example.BE.dto.admin.response.SessionResponse;
import com.example.BE.dto.admin.response.UserResponse;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.model.HotelModel;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    Page<UserResponse> getAllUsers(String keyword, String role, Boolean active, int page, int size, Authentication authentication);
    UserResponse getUserById(Long id);
    UserResponse changeRole(Long id, ChangeRoleRequest request, Authentication authentication);
    DashboardResponse getDashboardForAdmin();
    DashboardResponse getDashboard(Authentication authentication);
    UserResponse lockUser(Long id, Authentication authentication);
    UserResponse unlockUser(Long id, Authentication authentication);
    List<SessionResponse> getAllSessionById(Long id);
    SessionResponse getSessionById(UUID sessionId);
    List<BookingResponse> getAdminBookings(Authentication authentication);
    List<HotelModel> getAdminHotels(Authentication authentication);
}
