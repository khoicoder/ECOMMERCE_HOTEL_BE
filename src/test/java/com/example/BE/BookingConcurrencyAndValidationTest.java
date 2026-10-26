package com.example.BE;

import com.example.BE.dto.user.request.BookingRequest;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.Role;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.ConflictException;
import com.example.BE.repository.BookingRepository;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.services.Booking.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingConcurrencyAndValidationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        var existing = bookingRepository.findAll().stream()
                .filter(b -> b.getRoom() != null && (b.getRoom().getId() == 101L || b.getRoom().getId() == 102L))
                .toList();
        bookingRepository.deleteAll(existing);
    }

    private Authentication createAuth(Long userId, String username) {
        AuthPrincipal principal = new AuthPrincipal(userId, username, Role.USER, UUID.randomUUID());
        return new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of());
    }

    @Test
    @DisplayName("Race Condition Test: 2 người dùng đồng thời tranh chấp đặt cùng 1 phòng vào cùng khoảng ngày")
    void testConcurrentBookingRaceCondition() throws InterruptedException {
        Long testRoomId = 101L;
        LocalDate checkIn = LocalDate.of(2026, 12, 1);
        LocalDate checkOut = LocalDate.of(2026, 12, 5);

        BookingRequest requestUser1 = new BookingRequest(testRoomId, checkIn, checkOut);
        BookingRequest requestUser2 = new BookingRequest(testRoomId, checkIn, checkOut);

        Authentication authUser1 = createAuth(10L, "khachhang_vip");
        Authentication authUser2 = createAuth(11L, "le_hoang_nam");

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        // Luồng 1
        executor.submit(() -> {
            try {
                startLatch.await(); // Đợi hiệu lệnh xuất phát đồng thời
                BookingResponse res = bookingService.createBooking(requestUser1, authUser1);
                if (res != null) successCount.incrementAndGet();
            } catch (ConflictException ce) {
                conflictCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });

        // Luồng 2
        executor.submit(() -> {
            try {
                startLatch.await(); // Đợi hiệu lệnh xuất phát đồng thời
                BookingResponse res = bookingService.createBooking(requestUser2, authUser2);
                if (res != null) successCount.incrementAndGet();
            } catch (ConflictException ce) {
                conflictCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });

        // Kích hoạt đồng thời cả 2 luồng tại cùng một mili-giây
        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

        executor.shutdown();

        assertTrue(completed, "Các tiến trình đồng thời phải hoàn tất trong 10 giây");
        assertEquals(1, successCount.get(), "Chỉ DUY NHẤT 1 request đặt phòng thành công");
        assertEquals(1, conflictCount.get(), "Request cạnh tranh còn lại PHẢI bị chặn với ConflictException (HTTP 409)");
    }

    @Test
    @DisplayName("Business Validation: Không cho phép đặt phòng đang trong trạng thái MAINTENANCE")
    void testBookingMaintenanceRoomFails() {
        Long maintenanceRoomId = 206L; // Phòng 206 đang bảo trì
        BookingRequest request = new BookingRequest(maintenanceRoomId, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));
        Authentication auth = createAuth(10L, "khachhang_vip");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.createBooking(request, auth);
        });

        assertTrue(ex.getMessage().contains("bảo trì hoặc ngừng phục vụ"));
    }

    @Test
    @DisplayName("Date Range Validation: Ngày trả phòng trước hoặc bằng ngày nhận phòng phải bị từ chối")
    void testInvalidDateRangeFails() {
        Long testRoomId = 102L;
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(3); // Sai: checkOut trước checkIn
        BookingRequest request = new BookingRequest(testRoomId, checkIn, checkOut);
        Authentication auth = createAuth(10L, "khachhang_vip");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.createBooking(request, auth);
        });

        assertTrue(ex.getMessage().contains("phải sau ngày nhận phòng"));
    }

    @Test
    @DisplayName("Date Range Validation: Ngày nhận phòng trong quá khứ phải bị từ chối")
    void testPastCheckInDateFails() {
        Long testRoomId = 102L;
        LocalDate checkIn = LocalDate.now().minusDays(2); // Sai: ngày quá khứ
        LocalDate checkOut = LocalDate.now().plusDays(1);
        BookingRequest request = new BookingRequest(testRoomId, checkIn, checkOut);
        Authentication auth = createAuth(10L, "khachhang_vip");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            bookingService.createBooking(request, auth);
        });

        assertTrue(ex.getMessage().contains("không thể ở trong quá khứ"));
    }
}
