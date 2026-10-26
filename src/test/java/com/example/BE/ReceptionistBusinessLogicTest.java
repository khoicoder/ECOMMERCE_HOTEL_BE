package com.example.BE;

import com.example.BE.dto.user.request.CheckOutRequest;
import com.example.BE.dto.user.request.ProcessPaymentRequest;
import com.example.BE.dto.user.response.BillPreviewResponse;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.BookingStatus;
import com.example.BE.enums.PaymentMethod;
import com.example.BE.enums.PaymentStatus;
import com.example.BE.enums.Role;
import com.example.BE.enums.RoomStatus;
import com.example.BE.enums.StayStatus;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.ConflictException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.model.BookingModel;
import com.example.BE.model.HotelModel;
import com.example.BE.model.RoomModel;
import com.example.BE.model.UserModel;
import com.example.BE.repository.BookingRepository;
import com.example.BE.repository.HotelRepository;
import com.example.BE.repository.PaymentRepository;
import com.example.BE.repository.RoomRepository;
import com.example.BE.repository.UserRepository;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.services.Booking.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReceptionistBusinessLogicTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private HotelModel hotel1;
    private HotelModel hotel2;
    private RoomModel room101;
    private RoomModel room102;
    private RoomModel roomHotel2;
    private UserModel guestUser;
    private UserModel receptionistUser;
    private Authentication receptionistAuth;

    private final List<Long> createdBookingIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        hotel1 = hotelRepository.findById(1L).orElseThrow();
        hotel2 = hotelRepository.findById(2L).orElseGet(() -> {
            HotelModel h = new HotelModel();
            h.setName("Hotel 2 Branch");
            h.setLocation("Da Nang");
            return hotelRepository.save(h);
        });

        guestUser = userRepository.findById(10L).orElseGet(() -> {
            UserModel u = new UserModel();
            u.setUsername("guest_test_" + UUID.randomUUID().toString().substring(0, 6));
            u.setEmail(u.getUsername() + "@test.com");
            u.setRole(Role.USER);
            return userRepository.save(u);
        });

        receptionistUser = userRepository.findById(8L).orElseGet(() -> {
            UserModel u = new UserModel();
            u.setUsername("letan");
            u.setEmail("letan@test.com");
            u.setRole(Role.RECEPTIONIST);
            return userRepository.save(u);
        });

        // Dọn dẹp các đơn đặt phòng thử nghiệm cũ trên room101 & room102
        var oldBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getRoom() != null && (b.getRoom().getId() == 101L || b.getRoom().getId() == 102L))
                .toList();
        for (var b : oldBookings) {
            try {
                paymentRepository.findByBooking_IdOrderByPaidAtDesc(b.getId()).forEach(paymentRepository::delete);
                bookingRepository.delete(b);
            } catch (Exception ignored) {}
        }

        // Room 101 in Hotel 1
        room101 = roomRepository.findById(101L).orElseGet(() -> {
            RoomModel r = new RoomModel();
            r.setName("Phòng 101 Test");
            r.setType("Standard Single");
            r.setStatus(RoomStatus.AVAILABLE);
            r.setHotel(hotel1);
            return roomRepository.save(r);
        });
        room101.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room101);

        // Room 102 in Hotel 1
        room102 = roomRepository.findById(102L).orElseGet(() -> {
            RoomModel r = new RoomModel();
            r.setName("Phòng 102 Test");
            r.setType("Standard Single");
            r.setStatus(RoomStatus.AVAILABLE);
            r.setHotel(hotel1);
            return roomRepository.save(r);
        });
        room102.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room102);

        // Room in Hotel 2
        var roomsInHotel2 = roomRepository.findByHotel_Id(hotel2.getId());
        if (!roomsInHotel2.isEmpty()) {
            roomHotel2 = roomsInHotel2.get(0);
        } else {
            RoomModel r = new RoomModel();
            r.setName("Phòng H2-101");
            r.setType("Superior Double");
            r.setStatus(RoomStatus.AVAILABLE);
            r.setHotel(hotel2);
            roomHotel2 = roomRepository.save(r);
        }

        AuthPrincipal principal = new AuthPrincipal(receptionistUser.getId(), receptionistUser.getUsername(), Role.RECEPTIONIST, UUID.randomUUID());
        receptionistAuth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdBookingIds) {
            try {
                paymentRepository.findByBooking_IdOrderByPaidAtDesc(id).forEach(paymentRepository::delete);
                bookingRepository.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        createdBookingIds.clear();
        room101.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room101);
        room102.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room102);
    }

    private BookingModel createTestBooking(RoomModel room, BookingStatus status, StayStatus stayStatus, PaymentStatus paymentStatus,
                                           BigDecimal totalPrice, BigDecimal paidAmount, Instant deadline,
                                           LocalDate checkIn, LocalDate checkOut) {
        BookingModel b = new BookingModel();
        b.setUser(guestUser);
        b.setRoom(room);
        b.setStatus(status);
        b.setStayStatus(stayStatus != null ? stayStatus : StayStatus.NOT_CHECKED_IN);
        b.setPaymentStatus(paymentStatus != null ? paymentStatus : PaymentStatus.UNPAID);
        b.setTotalPrice(totalPrice);
        b.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        b.setRemainingAmount(totalPrice.subtract(b.getPaidAmount()));
        b.setExtraCharges(BigDecimal.ZERO);
        b.setPaymentDeadline(deadline);
        b.setCheckInDate(checkIn != null ? checkIn : LocalDate.now());
        b.setCheckOutDate(checkOut != null ? checkOut : LocalDate.now().plusDays(2));
        b.setCreatedAt(Instant.now());
        b.setUpdatedAt(Instant.now());
        BookingModel saved = bookingRepository.save(b);
        createdBookingIds.add(saved.getId());
        return saved;
    }

    @Test
    @DisplayName("Scenario 1: Đơn PENDING chưa quá hạn (deadline tương lai) không bị tự động hết hạn")
    void test1_PendingBookingWithinDeadline_DoesNotExpire() {
        Instant futureDeadline = Instant.now().plus(1, ChronoUnit.HOURS);
        BookingModel booking = createTestBooking(room101, BookingStatus.PENDING, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(1000000), BigDecimal.ZERO, futureDeadline,
                LocalDate.now(), LocalDate.now().plusDays(2));

        int expiredCount = bookingService.expireOverdueBookings();
        BookingModel reloaded = bookingRepository.findById(booking.getId()).orElseThrow();

        assertEquals(BookingStatus.PENDING, reloaded.getStatus(), "Đơn còn hạn không được chuyển sang EXPIRED");
        assertEquals(StayStatus.NOT_CHECKED_IN, reloaded.getStayStatus());
    }

    @Test
    @DisplayName("Scenario 2: Đơn PENDING đã quá hạn (deadline quá khứ) tự động chuyển sang EXPIRED khi scheduler quét")
    void test2_OverduePendingBooking_ExpiresOnScan() {
        Instant pastDeadline = Instant.now().minus(10, ChronoUnit.MINUTES);
        BookingModel booking = createTestBooking(room101, BookingStatus.PENDING, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(1000000), BigDecimal.ZERO, pastDeadline,
                LocalDate.now(), LocalDate.now().plusDays(2));

        int expiredCount = bookingService.expireOverdueBookings();
        BookingModel reloaded = bookingRepository.findById(booking.getId()).orElseThrow();

        assertEquals(BookingStatus.EXPIRED, reloaded.getStatus(), "Đơn quá hạn thanh toán phải chuyển sang EXPIRED");
        assertTrue(expiredCount >= 1, "Số lượng đơn hết hạn xử lý ít nhất là 1");
    }

    @Test
    @DisplayName("Scenario 3: Đơn CONFIRMED nhưng quá ngày check-in mà khách không đến tự động chuyển NO_SHOW")
    void test3_ConfirmedBooking_NoShowPastArrival() {
        LocalDate pastCheckIn = LocalDate.now().minusDays(2);
        LocalDate pastCheckOut = LocalDate.now().plusDays(1);
        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                pastCheckIn, pastCheckOut);

        bookingService.markNoShowBookings();
        BookingModel reloaded = bookingRepository.findById(booking.getId()).orElseThrow();

        assertEquals(BookingStatus.NO_SHOW, reloaded.getStatus(), "Khách không đến nhận phòng sau ngày check-in phải chuyển sang NO_SHOW");
    }

    @Test
    @DisplayName("Scenario 4: Đơn đã CHECKED_IN không bao giờ bị hủy hay hết hạn bởi background job")
    void test4_CheckedInBooking_NeverExpiresOrNoShows() {
        LocalDate pastCheckIn = LocalDate.now().minusDays(2);
        Instant pastDeadline = Instant.now().minus(2, ChronoUnit.DAYS);
        BookingModel booking = createTestBooking(room101, BookingStatus.CHECKED_IN, StayStatus.CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), pastDeadline,
                pastCheckIn, LocalDate.now().plusDays(1));

        bookingService.expireOverdueBookings();
        bookingService.markNoShowBookings();

        BookingModel reloaded = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(StayStatus.CHECKED_IN, reloaded.getStayStatus(), "Khách đang lưu trú không bao giờ bị hủy");
        assertEquals(BookingStatus.CHECKED_IN, reloaded.getStatus());
    }

    @Test
    @DisplayName("Scenario 5: Không thể Check-in cho đơn đặt phòng đã EXPIRED")
    void test5_ExpiredBooking_CannotCheckIn() {
        BookingModel booking = createTestBooking(room101, BookingStatus.EXPIRED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(1000000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.checkIn(booking.getId(), receptionistAuth);
        });
        assertTrue(ex.getMessage().contains("hết hạn"));
    }

    @Test
    @DisplayName("Scenario 6: Không thể Check-in cho đơn đặt phòng đã CANCELLED")
    void test6_CancelledBooking_CannotCheckIn() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CANCELLED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(1000000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.checkIn(booking.getId(), receptionistAuth);
        });
        assertTrue(ex.getMessage().contains("bị hủy"));
    }

    @Test
    @DisplayName("Scenario 7: Thanh toán tiền mặt mà không có xác nhận nhận tiền (cashConfirmed=false) bị từ chối")
    void test7_CashPayment_WithoutConfirmation_ThrowsBadRequest() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(500000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        ProcessPaymentRequest req = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(500000))
                .paymentMethod(PaymentMethod.CASH)
                .cashConfirmed(false)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.processReceptionistPayment(booking.getId(), req, receptionistAuth);
        });
        assertTrue(ex.getMessage().contains("nhận đủ tiền mặt") || ex.getMessage().contains("tiền mặt"));
    }

    @Test
    @DisplayName("Scenario 8: Thanh toán tiền mặt có xác nhận (cashConfirmed=true) thành công, cập nhật PAID")
    void test8_CashPayment_Success() {
        BookingModel booking = createTestBooking(room101, BookingStatus.PENDING, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(800000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        ProcessPaymentRequest req = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(800000))
                .paymentMethod(PaymentMethod.CASH)
                .cashConfirmed(true)
                .referenceNote("Khách nộp tiền mặt tại quầy số 1")
                .build();

        BookingResponse response = bookingService.processReceptionistPayment(booking.getId(), req, receptionistAuth);

        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus(), "Thanh toán đủ tự động chuyển đơn PENDING sang CONFIRMED");
        assertEquals(0, BigDecimal.valueOf(800000).compareTo(response.getPaidAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getRemainingAmount()));

        var payments = paymentRepository.findByBooking_IdOrderByPaidAtDesc(booking.getId());
        assertEquals(1, payments.size(), "Bản ghi PaymentModel phải được lưu vết trong DB");
        assertEquals(PaymentMethod.CASH, payments.get(0).getPaymentMethod());
        assertTrue(payments.get(0).getCashConfirmed());
    }

    @Test
    @DisplayName("Scenario 9: Thanh toán chuyển khoản không có mã giao dịch (transactionCode) bị từ chối")
    void test9_BankTransfer_WithoutTxnCode_ThrowsBadRequest() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(1000000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        ProcessPaymentRequest req = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(1000000))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .transactionCode(null)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.processReceptionistPayment(booking.getId(), req, receptionistAuth);
        });
        assertTrue(ex.getMessage().contains("mã giao dịch"));
    }

    @Test
    @DisplayName("Scenario 10: Trùng mã giao dịch thanh toán chuyển khoản bị từ chối với ConflictException")
    void test10_BankTransfer_DuplicateTxnCode_ThrowsConflict() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(1000000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        String uniqueTxnCode = "TXN_TEST_" + UUID.randomUUID().toString().substring(0, 8);

        ProcessPaymentRequest req1 = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(500000))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .transactionCode(uniqueTxnCode)
                .build();

        bookingService.processReceptionistPayment(booking.getId(), req1, receptionistAuth);

        ProcessPaymentRequest reqDuplicate = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(500000))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .transactionCode(uniqueTxnCode)
                .build();

        assertThrows(ConflictException.class, () -> {
            bookingService.processReceptionistPayment(booking.getId(), reqDuplicate, receptionistAuth);
        });
    }

    @Test
    @DisplayName("Scenario 11: Số tiền thanh toán âm hoặc bằng 0 bị từ chối")
    void test11_Payment_InvalidAmount_ZeroOrNegative() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(500000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        ProcessPaymentRequest zeroReq = ProcessPaymentRequest.builder()
                .amount(BigDecimal.ZERO)
                .paymentMethod(PaymentMethod.CASH)
                .cashConfirmed(true)
                .build();

        assertThrows(BadRequestException.class, () -> {
            bookingService.processReceptionistPayment(booking.getId(), zeroReq, receptionistAuth);
        });

        ProcessPaymentRequest negativeReq = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(-100000))
                .paymentMethod(PaymentMethod.CASH)
                .cashConfirmed(true)
                .build();

        assertThrows(BadRequestException.class, () -> {
            bookingService.processReceptionistPayment(booking.getId(), negativeReq, receptionistAuth);
        });
    }

    @Test
    @DisplayName("Scenario 12: Số tiền thanh toán vượt quá số dư còn nợ bị từ chối")
    void test12_Payment_ExceedingRemainingDebt_ThrowsBadRequest() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(500000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        ProcessPaymentRequest excessReq = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(600000)) // Nợ 500k, trả 600k
                .paymentMethod(PaymentMethod.CASH)
                .cashConfirmed(true)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.processReceptionistPayment(booking.getId(), excessReq, receptionistAuth);
        });
        assertTrue(ex.getMessage().contains("vượt quá số tiền còn nợ"));
    }

    @Test
    @DisplayName("Scenario 13: Luồng thanh toán nhiều lần (Thanh toán 1 phần PARTIALLY_PAID -> Thanh toán nốt PAID)")
    void test13_PartialPayment_Lifecycle() {
        BookingModel booking = createTestBooking(room101, BookingStatus.PENDING, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.UNPAID, BigDecimal.valueOf(1000000), BigDecimal.ZERO, null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        // Lần 1: Thanh toán cọc 400.000đ
        ProcessPaymentRequest partReq = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(400000))
                .paymentMethod(PaymentMethod.CASH)
                .cashConfirmed(true)
                .referenceNote("Đặt cọc 40%")
                .build();

        BookingResponse step1 = bookingService.processReceptionistPayment(booking.getId(), partReq, receptionistAuth);
        assertEquals(PaymentStatus.PARTIALLY_PAID, step1.getPaymentStatus());
        assertEquals(0, BigDecimal.valueOf(400000).compareTo(step1.getPaidAmount()));
        assertEquals(0, BigDecimal.valueOf(600000).compareTo(step1.getRemainingAmount()));

        // Lần 2: Thanh toán nốt 600.000đ
        ProcessPaymentRequest fullReq = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(600000))
                .paymentMethod(PaymentMethod.CASH)
                .cashConfirmed(true)
                .referenceNote("Thanh toán dứt điểm")
                .build();

        BookingResponse step2 = bookingService.processReceptionistPayment(booking.getId(), fullReq, receptionistAuth);
        assertEquals(PaymentStatus.PAID, step2.getPaymentStatus());
        assertEquals(0, BigDecimal.valueOf(1000000).compareTo(step2.getPaidAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(step2.getRemainingAmount()));
        assertEquals(BookingStatus.CONFIRMED, step2.getStatus());
    }

    @Test
    @DisplayName("Scenario 14: Check-in thành công: cập nhật stayStatus=CHECKED_IN, room.status=OCCUPIED, lưu actor")
    void test14_CheckIn_Success() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        BookingResponse res = bookingService.checkIn(booking.getId(), receptionistAuth);

        assertEquals(StayStatus.CHECKED_IN, res.getStayStatus());
        assertEquals(BookingStatus.CHECKED_IN, res.getStatus());
        assertNotNull(res.getActualCheckInTime());
        assertEquals("letan", res.getCheckedInBy());

        RoomModel updatedRoom = roomRepository.findById(room101.getId()).orElseThrow();
        assertEquals(RoomStatus.OCCUPIED, updatedRoom.getStatus(), "Phòng phải chuyển sang OCCUPIED khi khách nhận phòng");
    }

    @Test
    @DisplayName("Scenario 15: Không thể Check-in khi phòng vật lý đang OCCUPIED bởi khách khác")
    void test15_CheckIn_RoomOccupied_ThrowsConflict() {
        room101.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room101);

        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.checkIn(booking.getId(), receptionistAuth);
        });
        assertTrue(ex.getMessage().contains("đang có khách lưu trú"));
    }

    @Test
    @DisplayName("Scenario 16: Không thể Check-in khi phòng đang trong trạng thái MAINTENANCE (bảo trì)")
    void test16_CheckIn_RoomMaintenance_ThrowsBadRequest() {
        room101.setStatus(RoomStatus.MAINTENANCE);
        roomRepository.save(room101);

        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.checkIn(booking.getId(), receptionistAuth);
        });
        assertTrue(ex.getMessage().contains("bảo trì kỹ thuật"));
    }

    @Test
    @DisplayName("Scenario 17: Double Check-in (Check-in 2 lần cho cùng một đơn) bị từ chối với ConflictException")
    void test17_CheckIn_AlreadyCheckedIn_ThrowsConflict() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CHECKED_IN, StayStatus.CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        assertThrows(ConflictException.class, () -> {
            bookingService.checkIn(booking.getId(), receptionistAuth);
        });
    }

    @Test
    @DisplayName("Scenario 18: Không thể Check-out đơn đặt phòng khi khách chưa Check-in (NOT_CHECKED_IN)")
    void test18_CheckOut_NotCheckedIn_ThrowsBadRequest() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        assertThrows(BadRequestException.class, () -> {
            bookingService.checkOut(booking.getId(), new CheckOutRequest(), receptionistAuth);
        });
    }

    @Test
    @DisplayName("Scenario 19: Check-out thành công có phụ thu: tính tiền còn nợ, cập nhật CHECKED_OUT, phòng chuyển CLEANING")
    void test19_CheckOut_Success_WithExtraCharges_RoomCleaning() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CHECKED_IN, StayStatus.CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));
        room101.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room101);

        CheckOutRequest outReq = CheckOutRequest.builder()
                .extraCharges(BigDecimal.valueOf(250000)) // Phụ thu giặt là, minibar 250k
                .notes("Minibar 2 nước ngọt + giặt là 1 áo sơ mi")
                .build();

        BookingResponse res = bookingService.checkOut(booking.getId(), outReq, receptionistAuth);

        assertEquals(StayStatus.CHECKED_OUT, res.getStayStatus());
        assertEquals(BookingStatus.CHECKED_OUT, res.getStatus());
        assertEquals(0, BigDecimal.valueOf(250000).compareTo(res.getExtraCharges()));
        assertEquals(0, BigDecimal.valueOf(250000).compareTo(res.getRemainingAmount()), "Phụ thu chưa thanh toán phải phản ánh vào nợ còn lại");
        assertNotNull(res.getActualCheckOutTime());
        assertEquals("letan", res.getCheckedOutBy());

        RoomModel updatedRoom = roomRepository.findById(room101.getId()).orElseThrow();
        assertEquals(RoomStatus.CLEANING, updatedRoom.getStatus(), "Sau khi Check-out, phòng phải chuyển sang CLEANING để dọn dẹp");
    }

    @Test
    @DisplayName("Scenario 20: Double Check-out (Check-out 2 lần cho cùng một đơn) bị từ chối")
    void test20_CheckOut_AlreadyCheckedOut_ThrowsBadRequest() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CHECKED_OUT, StayStatus.CHECKED_OUT,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        assertThrows(BadRequestException.class, () -> {
            bookingService.checkOut(booking.getId(), new CheckOutRequest(), receptionistAuth);
        });
    }

    @Test
    @DisplayName("Scenario 21: Bill Preview tính toán chi tiết và chính xác công nợ trước khi xuất hóa đơn Check-out")
    void test21_BillPreview_AccurateCalculation() {
        BookingModel booking = createTestBooking(room101, BookingStatus.CHECKED_IN, StayStatus.CHECKED_IN,
                PaymentStatus.PARTIALLY_PAID, BigDecimal.valueOf(2000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));
        booking.setExtraCharges(BigDecimal.valueOf(300000));
        bookingRepository.save(booking);

        BillPreviewResponse bill = bookingService.getBillPreview(booking.getId(), receptionistAuth);

        assertEquals(0, BigDecimal.valueOf(2000000).compareTo(bill.getTotalRoomCharge()));
        assertEquals(0, BigDecimal.valueOf(300000).compareTo(bill.getExtraCharges()));
        assertEquals(0, BigDecimal.valueOf(2300000).compareTo(bill.getTotalBill()));
        assertEquals(0, BigDecimal.valueOf(1000000).compareTo(bill.getPaidAmount()));
        assertEquals(0, BigDecimal.valueOf(1300000).compareTo(bill.getRemainingAmount()), "Nợ còn lại phải = (Tiền phòng + Phụ thu) - Đã trả");
    }

    @Test
    @DisplayName("Scenario 22: Concurrency Race Condition - 2 lễ tân nhấn Check-in cùng lúc trên cùng 1 đơn, chỉ duy nhất 1 người thành công")
    void test22_ConcurrentCheckIn_RaceCondition() throws InterruptedException {
        BookingModel booking = createTestBooking(room102, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int workerId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    AuthPrincipal p = new AuthPrincipal(8L, "letan_" + workerId, Role.RECEPTIONIST, UUID.randomUUID());
                    Authentication auth = new UsernamePasswordAuthenticationToken(p, null, List.of());
                    BookingResponse res = bookingService.checkIn(booking.getId(), auth);
                    if (res != null) {
                        successCount.incrementAndGet();
                    }
                } catch (ConflictException ce) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    // unexpected
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Tiến trình đồng thời hoàn tất trong 10 giây");
        assertEquals(1, successCount.get(), "Chỉ DUY NHẤT 1 yêu cầu Check-in thành công");
        assertEquals(1, conflictCount.get(), "Yêu cầu cạnh tranh đồng thời còn lại PHẢI bị chặn với ConflictException");
    }

    @Test
    @DisplayName("Scenario 23: Lễ tân khách sạn 1 không được quyền Check-in hoặc xử lý thanh toán đơn thuộc khách sạn 2")
    void test23_CrossHotelAuthorization_Rejected() {
        // Đơn thuộc Khách sạn 2
        BookingModel bookingHotel2 = createTestBooking(roomHotel2, BookingStatus.CONFIRMED, StayStatus.NOT_CHECKED_IN,
                PaymentStatus.PAID, BigDecimal.valueOf(1500000), BigDecimal.valueOf(1500000), null,
                LocalDate.now(), LocalDate.now().plusDays(2));

        // Lễ tân user 8 trực thuộc Khách sạn 1
        assertThrows(UnauthorizedException.class, () -> {
            bookingService.checkIn(bookingHotel2.getId(), receptionistAuth);
        });

        ProcessPaymentRequest payReq = ProcessPaymentRequest.builder()
                .amount(BigDecimal.valueOf(500000))
                .paymentMethod(PaymentMethod.CASH)
                .cashConfirmed(true)
                .build();

        assertThrows(UnauthorizedException.class, () -> {
            bookingService.processReceptionistPayment(bookingHotel2.getId(), payReq, receptionistAuth);
        });
    }
}
