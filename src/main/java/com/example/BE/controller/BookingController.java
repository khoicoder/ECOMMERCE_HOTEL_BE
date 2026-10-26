package com.example.BE.controller;

import com.example.BE.dto.user.request.BookingRequest;
import com.example.BE.dto.user.request.CheckOutRequest;
import com.example.BE.dto.user.request.ProcessPaymentRequest;
import com.example.BE.dto.user.response.BillPreviewResponse;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.BookingStatus;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.services.Booking.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingService.findAllBookings(authentication));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request, authentication));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bookingService.findAllByUserId(principal.userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.mapToResponseBooking(bookingService.findById(id)));
    }

    @GetMapping("/{id}/bill")
    public ResponseEntity<BillPreviewResponse> getBillPreview(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBillPreview(id, authentication));
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<BookingResponse> checkIn(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.checkIn(id, authentication));
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<BookingResponse> checkOut(
            @PathVariable Long id,
            @RequestBody(required = false) CheckOutRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(bookingService.checkOut(id, request, authentication));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<BookingResponse> processPayment(
            @PathVariable Long id,
            @Valid @RequestBody ProcessPaymentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(bookingService.processReceptionistPayment(id, request, authentication));
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<BookingResponse> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String paymentStatus = body.getOrDefault("paymentStatus", "PAID");
        return ResponseEntity.ok(bookingService.updatePaymentStatus(id, paymentStatus, authentication));
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<BookingResponse> updatePaymentStatusPut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String paymentStatus = body.getOrDefault("paymentStatus", "PAID");
        return ResponseEntity.ok(bookingService.updatePaymentStatus(id, paymentStatus, authentication));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusBody,
            Authentication authentication
    ) {
        String statusStr = statusBody.get("status");
        BookingStatus status = BookingStatus.valueOf(statusStr.toUpperCase());
        return ResponseEntity.ok(bookingService.updateStatus(id, status, authentication));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatusPut(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusBody,
            Authentication authentication
    ) {
        String statusStr = statusBody.get("status");
        BookingStatus status = BookingStatus.valueOf(statusStr.toUpperCase());
        return ResponseEntity.ok(bookingService.updateStatus(id, status, authentication));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBookingPatch(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, authentication));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBookingPut(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, authentication));
    }

    @PostMapping("/cron/expire-overdue")
    public ResponseEntity<Map<String, Object>> triggerOverdueExpiration() {
        int expired = bookingService.expireOverdueBookings();
        int noShow = bookingService.markNoShowBookings();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "expiredBookingsCount", expired,
                "noShowBookingsCount", noShow
        ));
    }
}
