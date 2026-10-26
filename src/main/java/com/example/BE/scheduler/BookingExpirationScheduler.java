package com.example.BE.scheduler;

import com.example.BE.services.Booking.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final BookingService bookingService;

    /**
     * Tác vụ nền chạy định kỳ mỗi 60 giây:
     * - Tự động quét và đánh dấu EXPIRED cho các đơn PENDING quá hạn thanh toán
     * - Tự động giải phóng phòng đã giữ nếu không còn đơn nào khác trong ngày
     * - An toàn khi chạy lặp lại (Idempotent)
     */
    @Scheduled(fixedDelay = 60000)
    public void runOverdueBookingExpiration() {
        try {
            int expiredCount = bookingService.expireOverdueBookings();
            if (expiredCount > 0) {
                log.info("[BookingExpirationScheduler] Đã tự động hủy quá hạn {} đơn đặt phòng PENDING", expiredCount);
            }
        } catch (Exception e) {
            log.error("[BookingExpirationScheduler] Lỗi khi quét đơn quá hạn: {}", e.getMessage(), e);
        }
    }

    /**
     * Tác vụ nền quét các đơn CONFIRMED nhưng khách không đến nhận phòng (No-Show)
     * Chạy mỗi 10 phút một lần
     */
    @Scheduled(fixedDelay = 600000)
    public void runNoShowBookingScanner() {
        try {
            int noShowCount = bookingService.markNoShowBookings();
            if (noShowCount > 0) {
                log.info("[BookingExpirationScheduler] Đã tự động đánh dấu No-Show {} đơn đặt phòng", noShowCount);
            }
        } catch (Exception e) {
            log.error("[BookingExpirationScheduler] Lỗi khi quét đơn No-Show: {}", e.getMessage(), e);
        }
    }
}
