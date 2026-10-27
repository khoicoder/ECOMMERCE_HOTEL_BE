package com.example.BE;

import com.example.BE.dto.admin.request.CreateHotelAdminRequest;
import com.example.BE.dto.admin.request.CreateReceptionistRequest;
import com.example.BE.dto.admin.request.ResetStaffPasswordRequest;
import com.example.BE.dto.admin.response.HotelAdminResponse;
import com.example.BE.dto.admin.response.ReceptionistResponse;
import com.example.BE.enums.HotelStaffPosition;
import com.example.BE.enums.Role;
import com.example.BE.exception.ConflictException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.model.HotelModel;
import com.example.BE.model.HotelStaffModel;
import com.example.BE.model.UserModel;
import com.example.BE.repository.HotelRepository;
import com.example.BE.repository.HotelStaffRepository;
import com.example.BE.repository.UserRepository;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.services.AdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HotelStaffHierarchyTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelStaffRepository hotelStaffRepository;

    @Autowired
    private UserRepository userRepository;

    private Authentication superAdminAuth;
    private Authentication adminHotel1Auth;
    private Authentication regularUserAuth;

    private HotelModel hotel1;
    private HotelModel hotelWithoutAdmin;
    private UserModel admin1User;
    private final List<Long> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Super Admin Auth
        AuthPrincipal superPrincipal = new AuthPrincipal(1L, "admin", Role.SUPER_ADMIN, UUID.randomUUID());
        superAdminAuth = new UsernamePasswordAuthenticationToken(superPrincipal, null, List.of());

        // Hotel 1 has owner admin1User (id = 2L "khoi")
        hotel1 = hotelRepository.findById(1L).orElseThrow();
        admin1User = userRepository.findById(2L).orElseThrow();

        AuthPrincipal admin1Principal = new AuthPrincipal(admin1User.getId(), admin1User.getUsername(), Role.ADMIN, UUID.randomUUID());
        adminHotel1Auth = new UsernamePasswordAuthenticationToken(admin1Principal, null, List.of());

        // Regular User
        AuthPrincipal userPrincipal = new AuthPrincipal(10L, "khachhang_vip", Role.USER, UUID.randomUUID());
        regularUserAuth = new UsernamePasswordAuthenticationToken(userPrincipal, null, List.of());

        // Ensure we have a temporary hotel without admin for testing
        HotelModel h = new HotelModel();
        h.setName("Khách Sạn Test Tạm Thời");
        h.setLocation("Hạ Long Test");
        h.setStatus("TEST");
        h.setOwner(null); // chưa có admin
        hotelWithoutAdmin = hotelRepository.save(h);
    }

    @AfterEach
    void tearDown() {
        for (Long uid : createdUserIds) {
            try {
                hotelStaffRepository.findByUserId(uid).forEach(hotelStaffRepository::delete);
                userRepository.deleteById(uid);
            } catch (Exception ignored) {}
        }
        createdUserIds.clear();

        if (hotelWithoutAdmin != null && hotelWithoutAdmin.getId() != null) {
            try {
                hotelRepository.deleteById(hotelWithoutAdmin.getId());
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("Quy tắc 1: Mỗi khách sạn chỉ có DUY NHẤT 1 Admin lớn. Chặn tạo thêm Admin cho Hotel đã có chủ")
    void testCannotCreateSecondAdminForHotelWithExistingOwner() {
        // Hotel 1 đã có owner là user 'khoi'
        assertNotNull(hotel1.getOwner(), "Khách sạn 1 phải đã có Admin lớn");

        CreateHotelAdminRequest req = CreateHotelAdminRequest.builder()
                .hotelId(hotel1.getId())
                .username("admin_duplicate_" + UUID.randomUUID().toString().substring(0, 5))
                .password("Password123@")
                .email("admin_dup@test.com")
                .build();

        ConflictException ex = assertThrows(ConflictException.class, () -> {
            adminService.createHotelAdmin(req, superAdminAuth);
        });

        assertTrue(ex.getMessage().contains("Mỗi khách sạn chỉ được có 1 Admin lớn"));
    }

    @Test
    @DisplayName("Quy tắc 2: Super Admin tạo thành công Admin lớn cho khách sạn chưa có người quản lý")
    void testSuperAdminCanCreateAdminForUnassignedHotel() {
        String uniqueUser = "admin_halong_" + UUID.randomUUID().toString().substring(0, 5);
        CreateHotelAdminRequest req = CreateHotelAdminRequest.builder()
                .hotelId(hotelWithoutAdmin.getId())
                .username(uniqueUser)
                .password("Password123@")
                .email(uniqueUser + "@hotel.com")
                .phone("0987111222")
                .build();

        HotelAdminResponse response = adminService.createHotelAdmin(req, superAdminAuth);
        createdUserIds.add(response.getAdminId());

        assertTrue(response.isHasAdmin());
        assertEquals(uniqueUser, response.getAdminUsername());

        HotelModel updated = hotelRepository.findById(hotelWithoutAdmin.getId()).orElseThrow();
        assertNotNull(updated.getOwner());
        assertEquals(uniqueUser, updated.getOwner().getUsername());
        assertEquals(Role.ADMIN, updated.getOwner().getRole());
    }

    @Test
    @DisplayName("Quy tắc 3: Admin khách sạn có thể tạo tài khoản Lễ tân riêng thuộc khách sạn của mình")
    void testHotelAdminCanCreateReceptionistForTheirHotel() {
        String uniqueReceptionist = "letan_test_" + UUID.randomUUID().toString().substring(0, 5);
        CreateReceptionistRequest req = CreateReceptionistRequest.builder()
                .username(uniqueReceptionist)
                .password("Password123@")
                .email(uniqueReceptionist + "@hotel.com")
                .phone("0909090909")
                .address("Quầy Sảnh A")
                .build();

        ReceptionistResponse res = adminService.createReceptionist(req, adminHotel1Auth);
        createdUserIds.add(res.getId());

        assertEquals(uniqueReceptionist, res.getUsername());
        assertEquals(hotel1.getId(), res.getHotelId());
        assertTrue(res.isActive());

        // Kiểm tra bản ghi hotel_staff_model
        List<HotelStaffModel> staffList = hotelStaffRepository.findByUserId(res.getId());
        assertEquals(1, staffList.size());
        assertEquals(hotel1.getId(), staffList.get(0).getHotel().getId());
        assertEquals(HotelStaffPosition.RECEPTIONIST, staffList.get(0).getStaffPosition());
    }

    @Test
    @DisplayName("Quy tắc 4: Admin khách sạn không thể quản lý hoặc đổi mật khẩu lễ tân của khách sạn khác")
    void testAdminCannotManageReceptionistOfAnotherHotel() {
        // Tạo lễ tân thuộc Khách sạn 2 (do Super Admin tạo)
        String otherReceptionist = "letan_hotel2_" + UUID.randomUUID().toString().substring(0, 5);
        CreateReceptionistRequest req = CreateReceptionistRequest.builder()
                .hotelId(2L)
                .username(otherReceptionist)
                .password("Password123@")
                .email(otherReceptionist + "@hotel.com")
                .build();

        ReceptionistResponse created = adminService.createReceptionist(req, superAdminAuth);
        createdUserIds.add(created.getId());

        // Admin khách sạn 1 cố gắng đổi mật khẩu hoặc khóa lễ tân khách sạn 2
        assertThrows(UnauthorizedException.class, () -> {
            adminService.toggleReceptionistStatus(created.getId(), adminHotel1Auth);
        });

        assertThrows(UnauthorizedException.class, () -> {
            adminService.resetReceptionistPassword(created.getId(), new ResetStaffPasswordRequest("NewPass123@"), adminHotel1Auth);
        });
    }

    @Test
    @DisplayName("Quy tắc 5: User thông thường không được phép tạo Admin hoặc Lễ tân")
    void testRegularUserCannotCreateAdminOrReceptionist() {
        CreateReceptionistRequest req = CreateReceptionistRequest.builder()
                .username("letan_hacker")
                .password("Password123@")
                .email("hacker@test.com")
                .build();

        assertThrows(UnauthorizedException.class, () -> {
            adminService.createReceptionist(req, regularUserAuth);
        });
    }
}
