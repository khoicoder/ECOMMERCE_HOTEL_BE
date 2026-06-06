package com.example.BE.services;

import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.DashboardResponse;
import com.example.BE.dto.admin.response.DashboardStats;
import com.example.BE.dto.admin.response.SessionResponse;
import com.example.BE.dto.admin.response.UserResponse;
import com.example.BE.enums.Role;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import com.example.BE.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final HotelRepository hotelRepository;
    private final DashboardRepository  dashboardRepository;
//    private final BookingRepository bookingRepository;

    @Override
    public Page<UserResponse> getAllUsers(String keyword, String role, boolean active, int page, int size) {
        Pageable  pageable =PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable).map(this :: mapToResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        UserModel user = userRepository.findById(id).orElseThrow(()
                -> new NotFoundException("User not found"+id));

        return mapToResponse(user);

    }
    @Override
    public UserResponse lockUser(@PathVariable Long id) {
        UserModel user =  userRepository.findById(id).orElseThrow(()
                -> new NotFoundException("User not found"+id));
        user.setActive(false);
        userRepository.save(user);
        return mapToResponse(user);
    }
    @Override
    public UserResponse unlockUser(@PathVariable Long id) {
        UserModel user =  userRepository.findById(id).orElseThrow(()
                -> new NotFoundException("User not found"+id));
        user.setActive(true);
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public DashboardResponse getDashboard() {
        DashboardStats stats = dashboardRepository.getDashboardStats();


        return new DashboardResponse(
                stats.getTotalUsers(),
                stats.getTotalAdmins(),
                stats.getActiveSessions(),
                stats.getTotalBookings(),
                stats.getTotalTodayBookings(),
                stats.getTotalHotel(),
                stats.getPendingBookings(),
                stats.getConfirmedBookings(),
                stats.getCancelledBookings(),
                stats.getCheckIns(),
                stats.getCheckOuts(),
                stats.getTotalRevenue()


        );
    }

    @Override
    public UserResponse changeRole(Long id,ChangeRoleRequest request) {
        UserModel user  = userRepository.findById(id).orElseThrow(()
                -> new NotFoundException("User not found"));
        if(user.getRole().equals(Role.SUPER_ADMIN)){
            System.out.println("HELLO AMIN");
        }
        if (user.getRole() == request.role()) {
            throw new BadRequestException(
                    "User already has role " + request.role()
            );
        }
        user.setRole(request.role());
        userRepository.save(user);
        return mapToResponse(user)
        ;
    }
    @Override
    public SessionResponse getSessionById(UUID sessionid) {
        UserSession userSession = userSessionRepository.findById(sessionid).orElseThrow(()->
                new NotFoundException("User not found"));
        return new SessionResponse(
                userSession.getId(),
                userSession.getCreateAt(),
                userSession.getLastUsedAt(),
                userSession.getRefreshTokenExpireAt(),
                userSession.getRevokedAt()


        );

    }


    @Override
    public List<SessionResponse> getAllSessionById(Long id) {
        UserModel user = userRepository.findById(id).orElseThrow(()
                -> new NotFoundException("User not found"));
        return userSessionRepository.findByUser(user).stream().map(session
                -> new SessionResponse(
                session.getId(),
                session.getCreateAt(),
                session.getLastUsedAt(),
                session.getRefreshTokenExpireAt(),
                session.getRevokedAt()
        )).toList() ;
    }

    private UserResponse mapToResponse(UserModel userModel) {
        return new UserResponse(
            userModel.getId(),
            userModel.getUsername(),
            userModel.getEmail(),
            userModel.getRole().name(),
            userModel.getPhone(),
            userModel.getAddress(),
            userModel.isActive(),
            userModel.getCreatedAt(),
            userModel.getUpdatedAt());

    }


}
