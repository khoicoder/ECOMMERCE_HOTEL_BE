package com.example.BE.services.Booking;

import com.example.BE.dto.user.request.BookingRequest;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.BookingStatus;
import com.example.BE.exception.NotFoundException;
import com.example.BE.model.BookingModel;
import com.example.BE.model.RoomModel;
import com.example.BE.model.UserModel;
import com.example.BE.repository.BookingRepository;
import com.example.BE.repository.RoomRepository;
import com.example.BE.repository.UserRepository;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final UserService userService;

    @Override
    public List<BookingModel> findAllBookings() {
        return List.of();
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Authentication authentication) {

        AuthPrincipal authPrincipal = (AuthPrincipal) authentication.getPrincipal();
        UserModel user = userRepository.findById(authPrincipal.userId()).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        RoomModel room = roomRepository.findById(request.roomId()).orElseThrow(()
        -> new NotFoundException("Room not found"));

        BookingModel booking = new BookingModel();
        booking.setUser(user);

        booking.setRoom(room);
        booking.setCreatedAt(Instant.now());
        booking.setStatus(BookingStatus.PENDING);


        booking.setCheckInDate(request.checkInDate().toLocalDate());
        booking.setCheckOutDate(request.checkOutDate().toLocalDate());
        BookingModel savedBooking = bookingRepository.save(booking);



        return mapToResponseBooking(savedBooking);
    }
    public BookingResponse mapToResponseBooking(BookingModel booking) {
        return new BookingResponse(
                booking.getId(),

                booking.getRoom() != null
                        ? booking.getRoom().getId()
                        : null,

                booking.getRoom() != null
                        ? booking.getRoom().getName()
                        : null,

                booking.getUser() != null
                        ? booking.getUser().getId()
                        : null,

                booking.getUser() != null
                        ? booking.getUser().getUsername()
                        : null,

                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalPrice(),
                booking.getStatus(),

                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );

    }

    @Override
    public BookingResponse updateBooking(BookingRequest request, Authentication authentication) {
        return null;
    }

    @Override
    public List<BookingModel> findBookingByStatus(BookingStatus status) {
        return List.of();
    }

    @Override
    public List<BookingModel> findBookingByHotelId(Long hotelId) {
        return List.of();
    }

    @Override
    public BookingModel findBookingById(Long id) {
        return null;
    }

    @Override
    public BookingModel findBookingByEmail(String email) {
        return null;
    }

    @Override
    public void deleteBooking(BookingModel booking) {

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public BookingModel findById(Long id) {
        return null;
    }

    @Override
    public List<BookingModel> findAllByUserId(Long id) {
        return List.of();
    }
}
