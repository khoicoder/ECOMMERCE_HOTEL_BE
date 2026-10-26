package com.example.BE.services;

import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.DashboardResponse;
import com.example.BE.dto.admin.response.SessionResponse;
import com.example.BE.dto.admin.response.UserResponse;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.BookingStatus;
import com.example.BE.enums.Role;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.model.BookingModel;
import com.example.BE.model.HotelModel;
import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import com.example.BE.repository.*;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.services.Booking.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final HotelRepository hotelRepository;
    private final HotelStaffRepository hotelStaffRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Override
    public Page<UserResponse> getAllUsers(
            String keyword,
            String roleStr,
            Boolean active,
            int page,
            int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Role role = (roleStr != null && !roleStr.isBlank()) ? Role.fromString(roleStr) : null;

        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            Role currentRole = principal.role();
            if (currentRole == Role.ADMIN || currentRole == Role.RECEPTIONIST) {
                // Regular Admin / Receptionist: only sees customers who booked at their hotel
                HotelModel ownedHotel = findOwnedHotel(principal.userId());
                if (ownedHotel == null) {
                    ownedHotel = hotelRepository.findById(1L).orElse(null);
                }
                if (ownedHotel == null) {
                    return new PageImpl<>(List.of(), pageable, 0);
                }
                List<UserModel> hotelUsers = userRepository.findUsersByHotelBookings(ownedHotel.getId());
                var mapped = hotelUsers.stream()
                        .filter(u -> keyword == null || keyword.isBlank() ||
                                u.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                                (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword.toLowerCase())))
                        .filter(u -> role == null || u.getRole() == role)
                        .filter(u -> active == null || u.isActive() == active)
                        .map(this::mapToResponse)
                        .toList();
                return new PageImpl<>(mapped, pageable, mapped.size());
            }
        }

        return userRepository.searchUsers(keyword, role, active, pageable).map(this::mapToResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        UserModel user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found: " + id));
        return mapToResponse(user);
    }

    @Override
    public UserResponse lockUser(Long id, Authentication authentication) {
        checkSuperAdminOnly(authentication, "khóa tài khoản");
        UserModel user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public UserResponse unlockUser(Long id, Authentication authentication) {
        checkSuperAdminOnly(authentication, "mở khóa tài khoản");
        UserModel user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found: " + id));
        user.setActive(true);
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public DashboardResponse getDashboardForAdmin() {
        return getDashboard(null);
    }

    @Override
    public DashboardResponse getDashboard(Authentication authentication) {
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        Role role = Role.SUPER_ADMIN;
        Long currentUserId = null;

        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            role = principal.role();
            currentUserId = principal.userId();
        }

        if (role == Role.SUPER_ADMIN) {
            // SUPER ADMIN: System-wide overall stats calculated from real database
            long totalUsers = userRepository.count();
            long totalAdmins = userRepository.countByRoleIn(List.of(Role.ADMIN, Role.SUPER_ADMIN));
            long activeSessions = userSessionRepository.countByRevokedAtIsNull();
            long totalBookings = bookingRepository.count();
            long totalTodayBookings = bookingRepository.countTodayBookings(today, startOfDay, endOfDay);
            long totalHotels = hotelRepository.count();
            long pending = bookingRepository.countByStatus(BookingStatus.PENDING);
            long confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
            long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);
            long checkedIn = bookingRepository.countByStatus(BookingStatus.CHECKED_IN);
            long checkedOut = bookingRepository.countByStatus(BookingStatus.CHECKED_OUT);
            BigDecimal totalRevenue = bookingRepository.sumTotalRevenue();

            return new DashboardResponse(
                    totalUsers,
                    totalAdmins,
                    activeSessions,
                    totalBookings,
                    totalTodayBookings,
                    totalHotels,
                    pending,
                    confirmed,
                    cancelled,
                    checkedIn,
                    checkedOut,
                    totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                    "SUPER_ADMIN",
                    null,
                    "Toàn Bộ Hệ Thống Khách Sạn"
            );
        } else {
            // REGULAR ADMIN / RECEPTIONIST: Only view data of the Hotel that they manage
            HotelModel ownedHotel = findOwnedHotel(currentUserId);
            if (ownedHotel == null) {
                ownedHotel = hotelRepository.findById(1L).orElse(null);
            }
            if (ownedHotel == null) {
                return new DashboardResponse(
                        0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
                        BigDecimal.ZERO, "ADMIN", null, "Chưa được phân quyền khách sạn nào"
                );
            }

            Long hotelId = ownedHotel.getId();
            long totalUsers = bookingRepository.countDistinctUsersByHotel(hotelId);
            long totalAdmins = 1;
            long activeSessions = userSessionRepository.countByUserIdAndRevokedAtIsNull(currentUserId);
            long totalBookings = bookingRepository.countByRoom_Hotel_Id(hotelId);
            long totalTodayBookings = bookingRepository.countTodayBookingsByHotel(hotelId, today, startOfDay, endOfDay);
            long totalHotels = 1;
            long pending = bookingRepository.countByRoom_Hotel_IdAndStatus(hotelId, BookingStatus.PENDING);
            long confirmed = bookingRepository.countByRoom_Hotel_IdAndStatus(hotelId, BookingStatus.CONFIRMED);
            long cancelled = bookingRepository.countByRoom_Hotel_IdAndStatus(hotelId, BookingStatus.CANCELLED);
            long checkedIn = bookingRepository.countByRoom_Hotel_IdAndStatus(hotelId, BookingStatus.CHECKED_IN);
            long checkedOut = bookingRepository.countByRoom_Hotel_IdAndStatus(hotelId, BookingStatus.CHECKED_OUT);
            BigDecimal totalRevenue = bookingRepository.sumRevenueByHotel(hotelId);

            return new DashboardResponse(
                    totalUsers,
                    totalAdmins,
                    activeSessions,
                    totalBookings,
                    totalTodayBookings,
                    totalHotels,
                    pending,
                    confirmed,
                    cancelled,
                    checkedIn,
                    checkedOut,
                    totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                    "ADMIN",
                    hotelId,
                    ownedHotel.getName()
            );
        }
    }

    @Override
    public List<BookingResponse> getAdminBookings(Authentication authentication) {
        return bookingService.findAllBookings(authentication);
    }

    @Override
    public List<HotelModel> getAdminHotels(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            Role role = principal.role();
            if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
                HotelModel owned = findOwnedHotel(principal.userId());
                if (owned == null) {
                    owned = hotelRepository.findById(1L).orElse(null);
                }
                return owned != null ? List.of(owned) : List.of();
            }
        }
        return hotelRepository.findAll();
    }

    @Override
    public UserResponse changeRole(Long id, ChangeRoleRequest request, Authentication authentication) {
        checkSuperAdminOnly(authentication, "thay đổi phân quyền");
        UserModel user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found: " + id));
        if (user.getRole() == request.role()) {
            throw new BadRequestException("User already has role " + request.role());
        }
        user.setRole(request.role());
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public SessionResponse getSessionById(UUID sessionId) {
        UserSession userSession = userSessionRepository.findById(sessionId).orElseThrow(
                () -> new NotFoundException("Session not found: " + sessionId));
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
        UserModel user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found: " + id));
        return userSessionRepository.findByUser(user).stream().map(session -> new SessionResponse(
                session.getId(),
                session.getCreateAt(),
                session.getLastUsedAt(),
                session.getRefreshTokenExpireAt(),
                session.getRevokedAt()
        )).toList();
    }

    private HotelModel findOwnedHotel(Long userId) {
        if (userId == null) return null;
        var opt = hotelRepository.findFirstByOwner_Id(userId);
        if (opt.isPresent()) return opt.get();
        var staffOpt = hotelStaffRepository.findFirstByUserId(userId);
        return staffOpt.map(com.example.BE.model.HotelStaffModel::getHotel).orElse(null);
    }

    private void checkSuperAdminOnly(Authentication authentication, String actionName) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new UnauthorizedException("Chưa xác thực");
        }
        Role role = principal.role();
        if (role != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Chỉ SUPER ADMIN mới có quyền " + actionName);
        }
    }

    private UserResponse mapToResponse(UserModel userModel) {
        return new UserResponse(
                userModel.getId(),
                userModel.getUsername(),
                userModel.getEmail(),
                userModel.getRole() != null ? userModel.getRole().name() : "USER",
                userModel.getPhone(),
                userModel.getAddress(),
                userModel.isActive(),
                userModel.getCreatedAt(),
                userModel.getUpdatedAt()
        );
    }
}
