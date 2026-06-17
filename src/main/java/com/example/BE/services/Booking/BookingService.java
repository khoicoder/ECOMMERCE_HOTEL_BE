package com.example.BE.services.Booking;

import com.example.BE.dto.user.request.BookingRequest;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.BookingStatus;
import com.example.BE.model.BookingModel;
import com.example.BE.repository.BookingRepository;
import org.springframework.security.core.Authentication;

import java.util.List;


public interface BookingService {
    BookingResponse createBooking(BookingRequest request, Authentication authentication);
    BookingResponse updateBooking(BookingRequest request, Authentication authentication);
    List<BookingModel> findBookingByStatus(BookingStatus status);
    List<BookingModel> findBookingByHotelId(Long hotelId);
    BookingModel findBookingById(Long id);
    BookingModel findBookingByEmail(String email);
    List<BookingModel> findAllBookings();
    BookingModel findById(Long id);
    List<BookingModel> findAllByUserId(Long id);
    void deleteBooking(BookingModel booking);

}
