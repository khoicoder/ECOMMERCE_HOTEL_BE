package com.example.BE.services.Booking;

import com.example.BE.dto.user.request.BookingRequest;
import com.example.BE.dto.user.request.CheckOutRequest;
import com.example.BE.dto.user.request.ProcessPaymentRequest;
import com.example.BE.dto.user.response.BillPreviewResponse;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.BookingStatus;
import com.example.BE.model.BookingModel;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, Authentication authentication);
    BookingResponse updateBooking(BookingRequest request, Authentication authentication);
    BookingResponse checkIn(Long id, Authentication authentication);
    BookingResponse checkOut(Long id, CheckOutRequest request, Authentication authentication);
    BookingResponse processReceptionistPayment(Long id, ProcessPaymentRequest request, Authentication authentication);
    BillPreviewResponse getBillPreview(Long id, Authentication authentication);
    BookingResponse cancelBooking(Long id, Authentication authentication);
    int expireOverdueBookings();
    int markNoShowBookings();

    // IDOR protection & tenant scoping
    BookingResponse getBookingById(Long id, Authentication authentication);

    // Backward compatible methods
    BookingResponse updateStatus(Long id, BookingStatus newStatus, Authentication authentication);
    BookingResponse updatePaymentStatus(Long id, String paymentStatus, Authentication authentication);

    List<BookingModel> findBookingByStatus(BookingStatus status);
    List<BookingModel> findBookingByHotelId(Long hotelId);
    BookingModel findBookingById(Long id);
    BookingModel findBookingByEmail(String email);
    List<BookingResponse> findAllBookings(Authentication authentication);
    BookingModel findById(Long id);
    List<BookingResponse> findAllByUserId(Long id);
    void deleteBooking(BookingModel booking);
    BookingResponse mapToResponseBooking(BookingModel booking);
}
