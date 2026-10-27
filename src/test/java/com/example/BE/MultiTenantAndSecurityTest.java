package com.example.BE;

import com.example.BE.dto.admin.response.DashboardResponse;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.BookingStatus;
import com.example.BE.enums.HotelStaffPosition;
import com.example.BE.enums.PaymentStatus;
import com.example.BE.enums.Role;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.model.*;
import com.example.BE.repository.*;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.security.JwtUtil;
import com.example.BE.security.TenantContext;
import com.example.BE.services.AdminService;
import com.example.BE.services.Booking.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MultiTenantAndSecurityTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelStaffRepository hotelStaffRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    private TenantModel tenantA;
    private TenantModel tenantB;
    private HotelModel hotelA;
    private HotelModel hotelB;
    private UserModel userCustomer;
    private UserModel userOtherCustomer;
    private UserModel adminHotelA;
    private UserModel adminHotelB;
    private UserModel unassignedAdmin;
    private UserModel superAdmin;

    @BeforeEach
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        // 1. Create two separate tenants
        tenantA = new TenantModel();
        tenantA.setCode("TENANT_A_" + uid);
        tenantA.setName("Tenant Organization A");
        tenantA.setStatus("ACTIVE");
        tenantA = tenantRepository.save(tenantA);

        tenantB = new TenantModel();
        tenantB.setCode("TENANT_B_" + uid);
        tenantB.setName("Tenant Organization B");
        tenantB.setStatus("ACTIVE");
        tenantB = tenantRepository.save(tenantB);

        // 2. Create hotels under different tenants
        hotelA = new HotelModel();
        hotelA.setName("Hotel Alpha");
        hotelA.setLocation("Hanoi, Vietnam");
        hotelA.setTenant(tenantA);
        hotelA = hotelRepository.save(hotelA);

        hotelB = new HotelModel();
        hotelB.setName("Hotel Beta");
        hotelB.setLocation("Da Nang, Vietnam");
        hotelB.setTenant(tenantB);
        hotelB = hotelRepository.save(hotelB);

        // 3. Create users
        userCustomer = new UserModel();
        userCustomer.setUsername("cust_" + uid);
        userCustomer.setPassword("pwd");
        userCustomer.setRole(Role.USER);
        userCustomer.setActive(true);
        userCustomer.setTenant(tenantA);
        userCustomer = userRepository.save(userCustomer);

        userOtherCustomer = new UserModel();
        userOtherCustomer.setUsername("other_" + uid);
        userOtherCustomer.setPassword("pwd");
        userOtherCustomer.setRole(Role.USER);
        userOtherCustomer.setActive(true);
        userOtherCustomer = userRepository.save(userOtherCustomer);

        adminHotelA = new UserModel();
        adminHotelA.setUsername("adminA_" + uid);
        adminHotelA.setPassword("pwd");
        adminHotelA.setRole(Role.ADMIN);
        adminHotelA.setActive(true);
        adminHotelA.setTenant(tenantA);
        adminHotelA = userRepository.save(adminHotelA);

        HotelStaffModel staffA = new HotelStaffModel();
        staffA.setUser(adminHotelA);
        staffA.setHotel(hotelA);
        staffA.setStaffPosition(HotelStaffPosition.HOTEL_MANAGER);
        hotelStaffRepository.save(staffA);

        adminHotelB = new UserModel();
        adminHotelB.setUsername("adminB_" + uid);
        adminHotelB.setPassword("pwd");
        adminHotelB.setRole(Role.ADMIN);
        adminHotelB.setActive(true);
        adminHotelB.setTenant(tenantB);
        adminHotelB = userRepository.save(adminHotelB);

        HotelStaffModel staffB = new HotelStaffModel();
        staffB.setUser(adminHotelB);
        staffB.setHotel(hotelB);
        staffB.setStaffPosition(HotelStaffPosition.HOTEL_MANAGER);
        hotelStaffRepository.save(staffB);

        unassignedAdmin = new UserModel();
        unassignedAdmin.setUsername("unassigned_" + uid);
        unassignedAdmin.setPassword("pwd");
        unassignedAdmin.setRole(Role.ADMIN);
        unassignedAdmin.setActive(true);
        unassignedAdmin = userRepository.save(unassignedAdmin);

        superAdmin = new UserModel();
        superAdmin.setUsername("super_" + uid);
        superAdmin.setPassword("pwd");
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setActive(true);
        superAdmin = userRepository.save(superAdmin);
    }

    private Authentication createAuth(UserModel user, String tenantCode) {
        AuthPrincipal principal = new AuthPrincipal(user.getId(), user.getUsername(), user.getRole(), UUID.randomUUID(), tenantCode);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    @Test
    @DisplayName("Tenant Isolation: Hotel repository filters strictly by tenant code")
    void testTenantDataIsolation() {
        List<HotelModel> hotelsOfTenantA = hotelRepository.findByTenant_Code(tenantA.getCode());
        assertEquals(1, hotelsOfTenantA.size());
        assertEquals("Hotel Alpha", hotelsOfTenantA.get(0).getName());

        List<HotelModel> hotelsOfTenantB = hotelRepository.findByTenant_Code(tenantB.getCode());
        assertEquals(1, hotelsOfTenantB.size());
        assertEquals("Hotel Beta", hotelsOfTenantB.get(0).getName());
    }

    @Test
    @DisplayName("TenantContext: ThreadLocal correctly manages tenant scope and dedicated flag")
    void testTenantContextManagement() {
        TenantContext.clear();
        assertEquals("DEFAULT", TenantContext.getTenant());

        TenantContext.setTenant("CUSTOM_TENANT");
        assertEquals("CUSTOM_TENANT", TenantContext.getTenant());

        TenantContext.clear();
        assertEquals("DEFAULT", TenantContext.getTenant());

        TenantContext.setDedicatedMode(true);
        assertTrue(TenantContext.isDedicatedMode());
        TenantContext.setDedicatedMode(false);
        assertFalse(TenantContext.isDedicatedMode());
    }

    @Test
    @DisplayName("Security IDOR Fix: Booking access restricted to owner, assigned admin, and super admin")
    void testBookingIdorProtection() {
        // Create room in Hotel A
        RoomModel room = new RoomModel();
        room.setName("Room 101");
        room.setType("DELUXE");
        room.setHotel(hotelA);
        room.setPrice(BigDecimal.valueOf(100));
        room = roomRepository.save(room);

        // Customer creates booking in Hotel A
        BookingModel booking = new BookingModel();
        booking.setUser(userCustomer);
        booking.setRoom(room);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(3));
        booking.setTotalPrice(BigDecimal.valueOf(200));
        booking.setPaidAmount(BigDecimal.valueOf(200));
        booking.setRemainingAmount(BigDecimal.ZERO);
        booking = bookingRepository.save(booking);

        Long bookingId = booking.getId();

        // 1. Owner can access
        Authentication customerAuth = createAuth(userCustomer, tenantA.getCode());
        BookingResponse respOwner = bookingService.getBookingById(bookingId, customerAuth);
        assertNotNull(respOwner);
        assertEquals(bookingId, respOwner.getId());

        // 2. Other customer is DENIED (Prevent IDOR)
        Authentication otherAuth = createAuth(userOtherCustomer, null);
        assertThrows(UnauthorizedException.class, () ->
                bookingService.getBookingById(bookingId, otherAuth)
        );

        // 3. Hotel A Admin can access
        Authentication adminAAuth = createAuth(adminHotelA, tenantA.getCode());
        BookingResponse respAdminA = bookingService.getBookingById(bookingId, adminAAuth);
        assertNotNull(respAdminA);

        // 4. Hotel B Admin is DENIED cross-tenant/cross-hotel access
        Authentication adminBAuth = createAuth(adminHotelB, tenantB.getCode());
        assertThrows(UnauthorizedException.class, () ->
                bookingService.getBookingById(bookingId, adminBAuth)
        );

        // 5. Super Admin can access
        Authentication superAuth = createAuth(superAdmin, null);
        BookingResponse respSuper = bookingService.getBookingById(bookingId, superAuth);
        assertNotNull(respSuper);
    }

    @Test
    @DisplayName("Hotel Scoping: Unassigned admin cannot fall back to Hotel 1 or leak other hotels")
    void testUnassignedAdminNoFallback() {
        Authentication unassignedAuth = createAuth(unassignedAdmin, null);

        // Calling getAdminHotels should return EMPTY, not fall back to Hotel 1
        List<HotelModel> hotels = adminService.getAdminHotels(unassignedAuth);
        assertTrue(hotels.isEmpty(), "Unassigned admin must see 0 hotels, never fall back to Hotel 1");

        // Calling getDashboard should return safe zeroed statistics, not Hotel 1
        DashboardResponse dashboard = adminService.getDashboard(unassignedAuth);
        assertNotNull(dashboard);
        assertEquals(0L, dashboard.totalHotels());
    }

    @Test
    @DisplayName("JWT: Token correctly encodes and decodes tenantCode (tid claim)")
    void testJwtTenantTokenClaim() {
        String token = jwtUtil.generateAccessToken(userCustomer, UUID.randomUUID());
        assertNotNull(token);

        String extractedTenant = jwtUtil.extractTenantCode(token);
        assertEquals(tenantA.getCode(), extractedTenant);
    }
}
