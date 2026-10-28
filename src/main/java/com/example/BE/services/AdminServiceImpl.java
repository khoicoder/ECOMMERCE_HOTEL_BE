package com.example.BE.services;

import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.request.CreateHotelAdminRequest;
import com.example.BE.dto.admin.request.CreateReceptionistRequest;
import com.example.BE.dto.admin.request.ResetStaffPasswordRequest;
import com.example.BE.dto.admin.response.*;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.BookingStatus;
import com.example.BE.enums.HotelStaffPosition;
import com.example.BE.enums.Role;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.ConflictException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.model.*;
import com.example.BE.repository.*;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.security.SessionCacheService;
import com.example.BE.services.Booking.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private final RoomRepository roomRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionCacheService sessionCacheService;

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

        // Immediate session purge across MySQL and Redis
        userSessionRepository.revokeAllActiveSessions(id, Instant.now());
        sessionCacheService.revokeSessions(
                userSessionRepository.findByUserId(id).stream().map(UserSession::getId).toList()
        );

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
                return new DashboardResponse(
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
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
    @Transactional(readOnly = true)
    public List<HotelModel> getAdminHotels(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            Role role = principal.role();
            if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
                HotelModel owned = findOwnedHotel(principal.userId());
                if (owned != null) {
                    if (owned.getRooms() != null) {
                        owned.getRooms().size();
                    }
                    return List.of(owned);
                }
                return List.of();
            }
        }
        List<HotelModel> hotels = hotelRepository.findAll();
        for (HotelModel h : hotels) {
            if (h.getRooms() != null) {
                h.getRooms().size();
            }
        }
        return hotels;
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

    private void logAudit(Long actorId, String actorUsername, String action, String description, String entityType, Long entityId) {
        try {
            AuditLogModel log = new AuditLogModel();
            log.setActorId(actorId);
            log.setActorUsername(actorUsername);
            log.setAction(action);
            log.setActionName(action);
            log.setDescription(description);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setCreatedAt(Instant.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Audit log error: " + e.getMessage());
        }
    }

    private String extractUsername(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal p) {
            return p.username();
        }
        return "System";
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal p) {
            return p.userId();
        }
        return null;
    }

    // =========================================================================
    // PHÂN CẤP HOTEL ADMIN (SUPER_ADMIN QUẢN LÝ ADMIN LỚN CỦA MỖI HOTEL)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<HotelAdminResponse> getHotelAdmins(Authentication authentication) {
        checkSuperAdminOnly(authentication, "xem danh sách Quản trị viên Khách sạn");
        List<HotelModel> hotels = hotelRepository.findAll();
        List<HotelAdminResponse> responses = new ArrayList<>();

        for (HotelModel hotel : hotels) {
            UserModel owner = hotel.getOwner();
            long roomCount = roomRepository.countByHotel_Id(hotel.getId());
            responses.add(HotelAdminResponse.builder()
                    .hotelId(hotel.getId())
                    .hotelName(hotel.getName())
                    .hotelLocation(hotel.getLocation())
                    .hasAdmin(owner != null)
                    .adminId(owner != null ? owner.getId() : null)
                    .adminUsername(owner != null ? owner.getUsername() : null)
                    .adminEmail(owner != null ? owner.getEmail() : null)
                    .adminPhone(owner != null ? owner.getPhone() : null)
                    .adminActive(owner != null ? owner.isActive() : null)
                    .roomCount(roomCount)
                    .build());
        }
        return responses;
    }

    @Override
    @Transactional
    public HotelAdminResponse createHotelAdmin(CreateHotelAdminRequest request, Authentication authentication) {
        checkSuperAdminOnly(authentication, "tạo tài khoản Quản trị viên Khách sạn");

        HotelModel hotel = hotelRepository.findById(request.getHotelId()).orElseThrow(
                () -> new NotFoundException("Khách sạn không tồn tại với ID: " + request.getHotelId())
        );

        // Quy tắc nghiệp vụ: Mỗi khách sạn chỉ có DUY NHẤT 1 ADMIN lớn!
        if (hotel.getOwner() != null) {
            throw new ConflictException("Khách sạn '" + hotel.getName() + "' hiện đã có Admin lớn quản lý: @"
                    + hotel.getOwner().getUsername() + ". Mỗi khách sạn chỉ được có 1 Admin lớn!");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại trên hệ thống!");
        }

        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email '" + request.getEmail() + "' đã được sử dụng!");
        }

        UserModel admin = new UserModel();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setAddress(request.getAddress());
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        UserModel savedAdmin = userRepository.save(admin);

        hotel.setOwner(savedAdmin);
        hotelRepository.save(hotel);

        logAudit(extractUserId(authentication), extractUsername(authentication), "CREATE_HOTEL_ADMIN",
                "Tạo tài khoản Admin lớn @" + savedAdmin.getUsername() + " quản lý khách sạn " + hotel.getName(),
                "UserModel", savedAdmin.getId());

        return HotelAdminResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelLocation(hotel.getLocation())
                .hasAdmin(true)
                .adminId(savedAdmin.getId())
                .adminUsername(savedAdmin.getUsername())
                .adminEmail(savedAdmin.getEmail())
                .adminPhone(savedAdmin.getPhone())
                .adminActive(savedAdmin.isActive())
                .roomCount(roomRepository.countByHotel_Id(hotel.getId()))
                .build();
    }

    @Override
    @Transactional
    public HotelAdminResponse assignHotelAdmin(Long hotelId, Long adminUserId, Authentication authentication) {
        checkSuperAdminOnly(authentication, "gán quyền Quản trị viên Khách sạn");

        HotelModel hotel = hotelRepository.findById(hotelId).orElseThrow(
                () -> new NotFoundException("Khách sạn không tồn tại: " + hotelId)
        );

        UserModel user = userRepository.findById(adminUserId).orElseThrow(
                () -> new NotFoundException("Tài khoản người dùng không tồn tại: " + adminUserId)
        );

        user.setRole(Role.ADMIN);
        userRepository.save(user);

        hotel.setOwner(user);
        hotelRepository.save(hotel);

        logAudit(extractUserId(authentication), extractUsername(authentication), "ASSIGN_HOTEL_ADMIN",
                "Chuyển nhượng quyền Admin lớn @" + user.getUsername() + " cho khách sạn " + hotel.getName(),
                "HotelModel", hotel.getId());

        return HotelAdminResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelLocation(hotel.getLocation())
                .hasAdmin(true)
                .adminId(user.getId())
                .adminUsername(user.getUsername())
                .adminEmail(user.getEmail())
                .adminPhone(user.getPhone())
                .adminActive(user.isActive())
                .roomCount(roomRepository.countByHotel_Id(hotel.getId()))
                .build();
    }

    // =========================================================================
    // PHÂN CẤP LỄ TÂN (ADMIN CỦA HOTEL QUẢN LÝ LỄ TÂN KHÁCH SẠN MÌNH)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionistResponse> getReceptionists(Long hotelId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new UnauthorizedException("Vui lòng đăng nhập để xem danh sách lễ tân");
        }

        Long targetHotelId = hotelId;
        if (principal.role() == Role.ADMIN) {
            HotelModel owned = findOwnedHotel(principal.userId());
            if (owned == null) {
                return java.util.Collections.emptyList();
            }
            targetHotelId = owned.getId();
        } else if (principal.role() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Chỉ Admin khách sạn hoặc Super Admin mới có quyền xem danh sách lễ tân");
        }

        List<HotelStaffModel> staffList;
        if (targetHotelId != null) {
            staffList = hotelStaffRepository.findByHotel_IdAndStaffPosition(targetHotelId, HotelStaffPosition.RECEPTIONIST);
        } else {
            staffList = hotelStaffRepository.findAll().stream()
                    .filter(s -> s.getStaffPosition() == HotelStaffPosition.RECEPTIONIST)
                    .toList();
        }

        return staffList.stream().map(s -> {
            UserModel u = s.getUser();
            HotelModel h = s.getHotel();
            return ReceptionistResponse.builder()
                    .id(u != null ? u.getId() : null)
                    .staffId(s.getId())
                    .username(u != null ? u.getUsername() : "")
                    .email(u != null ? u.getEmail() : "")
                    .phone(u != null ? u.getPhone() : "")
                    .address(u != null ? u.getAddress() : "")
                    .active(u != null && u.isActive())
                    .hotelId(h != null ? h.getId() : null)
                    .hotelName(h != null ? h.getName() : "")
                    .createdAt(u != null ? u.getCreatedAt() : null)
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public ReceptionistResponse createReceptionist(CreateReceptionistRequest request, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new UnauthorizedException("Vui lòng đăng nhập để tạo tài khoản Lễ tân");
        }

        Long targetHotelId = request.getHotelId();
        if (principal.role() == Role.ADMIN) {
            HotelModel owned = findOwnedHotel(principal.userId());
            if (owned == null) {
                throw new BadRequestException("Tài khoản của bạn chưa được gán quản lý khách sạn nào");
            }
            targetHotelId = owned.getId();
        } else if (principal.role() == Role.SUPER_ADMIN) {
            if (targetHotelId == null) {
                throw new BadRequestException("Vui lòng chọn khách sạn cần bổ nhiệm Lễ tân");
            }
        } else {
            throw new UnauthorizedException("Bạn không có quyền tạo tài khoản Lễ tân");
        }

        final Long finalHotelId = targetHotelId;
        HotelModel hotel = hotelRepository.findById(finalHotelId).orElseThrow(
                () -> new NotFoundException("Khách sạn không tồn tại với ID: " + finalHotelId)
        );

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại!");
        }

        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email '" + request.getEmail() + "' đã được sử dụng!");
        }

        UserModel user = new UserModel();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(Role.RECEPTIONIST);
        user.setActive(true);
        if (hotel.getTenant() != null) {
            user.setTenant(hotel.getTenant());
        }
        UserModel savedUser = userRepository.save(user);

        HotelStaffModel staff = new HotelStaffModel();
        staff.setUser(savedUser);
        staff.setHotel(hotel);
        staff.setStaffPosition(HotelStaffPosition.RECEPTIONIST);
        HotelStaffModel savedStaff = hotelStaffRepository.save(staff);

        logAudit(principal.userId(), principal.username(), "CREATE_RECEPTIONIST",
                "Tạo tài khoản Lễ tân @" + savedUser.getUsername() + " cho khách sạn " + hotel.getName(),
                "UserModel", savedUser.getId());

        return ReceptionistResponse.builder()
                .id(savedUser.getId())
                .staffId(savedStaff.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .address(savedUser.getAddress())
                .active(savedUser.isActive())
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public ReceptionistResponse toggleReceptionistStatus(Long userId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new UnauthorizedException("Vui lòng đăng nhập");
        }

        UserModel user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Tài khoản Lễ tân không tồn tại: " + userId)
        );

        if (user.getRole() != Role.RECEPTIONIST) {
            throw new BadRequestException("Tài khoản này không phải là Lễ tân");
        }

        var staffOpt = hotelStaffRepository.findFirstByUserId(userId);
        if (staffOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy thông tin bổ nhiệm của Lễ tân");
        }
        HotelStaffModel staff = staffOpt.get();

        if (principal.role() == Role.ADMIN) {
            HotelModel owned = findOwnedHotel(principal.userId());
            if (owned == null || staff.getHotel() == null || !staff.getHotel().getId().equals(owned.getId())) {
                throw new UnauthorizedException("Bạn chỉ có quyền quản lý Lễ tân thuộc khách sạn của mình");
            }
        } else if (principal.role() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Bạn không có quyền quản lý tài khoản Lễ tân");
        }

        user.setActive(!user.isActive());
        userRepository.save(user);

        // Nếu khóa tài khoản, lập tức thu hồi mọi session đang hoạt động
        if (!user.isActive()) {
            userSessionRepository.revokeAllActiveSessions(userId, Instant.now());
            sessionCacheService.revokeSessions(
                    userSessionRepository.findByUserId(userId).stream().map(UserSession::getId).toList()
            );
        }

        logAudit(principal.userId(), principal.username(), user.isActive() ? "UNLOCK_RECEPTIONIST" : "LOCK_RECEPTIONIST",
                (user.isActive() ? "Mở khóa" : "Khóa") + " Lễ tân @" + user.getUsername(), "UserModel", user.getId());

        return ReceptionistResponse.builder()
                .id(user.getId())
                .staffId(staff.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .active(user.isActive())
                .hotelId(staff.getHotel() != null ? staff.getHotel().getId() : null)
                .hotelName(staff.getHotel() != null ? staff.getHotel().getName() : "")
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void resetReceptionistPassword(Long userId, ResetStaffPasswordRequest request, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new UnauthorizedException("Vui lòng đăng nhập");
        }

        UserModel user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Tài khoản Lễ tân không tồn tại: " + userId)
        );

        if (user.getRole() != Role.RECEPTIONIST) {
            throw new BadRequestException("Tài khoản này không phải là Lễ tân");
        }

        var staffOpt = hotelStaffRepository.findFirstByUserId(userId);
        if (staffOpt.isEmpty()) {
            throw new NotFoundException("Không tìm thấy thông tin bổ nhiệm của Lễ tân");
        }
        HotelStaffModel staff = staffOpt.get();

        if (principal.role() == Role.ADMIN) {
            HotelModel owned = findOwnedHotel(principal.userId());
            if (owned == null || staff.getHotel() == null || !staff.getHotel().getId().equals(owned.getId())) {
                throw new UnauthorizedException("Bạn chỉ có quyền đổi mật khẩu cho Lễ tân thuộc khách sạn của mình");
            }
        } else if (principal.role() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Bạn không có quyền đặt lại mật khẩu Lễ tân");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Thu hồi toàn bộ session cũ để bắt buộc đăng nhập bằng mật khẩu mới
        userSessionRepository.revokeAllActiveSessions(userId, Instant.now());
        sessionCacheService.revokeSessions(
                userSessionRepository.findByUserId(userId).stream().map(UserSession::getId).toList()
        );

        logAudit(principal.userId(), principal.username(), "RESET_PASSWORD_RECEPTIONIST",
                "Đổi mật khẩu cho Lễ tân @" + user.getUsername(), "UserModel", user.getId());
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
