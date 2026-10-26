package com.example.BE.repository;

import com.example.BE.enums.BookingStatus;
import com.example.BE.model.BookingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingModel, Long> {

    @Query(value = "SELECT * FROM bookings WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<BookingModel> findByIdWithLock(@Param("id") Long id);

    @Query("""
        SELECT COUNT(b) > 0
        FROM BookingModel b
        WHERE b.room.id = :roomId
        AND b.status NOT IN (com.example.BE.enums.BookingStatus.CANCELLED, com.example.BE.enums.BookingStatus.EXPIRED)
        AND b.checkInDate < :checkOutDate
        AND b.checkOutDate > :checkInDate
    """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    @Query("""
        SELECT COUNT(b) > 0
        FROM BookingModel b
        WHERE b.user.id = :userId
        AND b.room.id = :roomId
        AND b.checkInDate = :checkInDate
        AND b.checkOutDate = :checkOutDate
        AND b.status NOT IN (com.example.BE.enums.BookingStatus.CANCELLED, com.example.BE.enums.BookingStatus.EXPIRED)
    """)
    boolean existsDuplicateBooking(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    List<BookingModel> findAllByOrderByCreatedAtDesc();

    List<BookingModel> findByRoom_Hotel_IdOrderByCreatedAtDesc(Long hotelId);

    List<BookingModel> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByRoom_Hotel_Id(Long hotelId);

    long countByRoom_Hotel_IdAndStatus(Long hotelId, BookingStatus status);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM BookingModel b WHERE b.status IN (com.example.BE.enums.BookingStatus.CONFIRMED, com.example.BE.enums.BookingStatus.CHECKED_IN, com.example.BE.enums.BookingStatus.CHECKED_OUT, com.example.BE.enums.BookingStatus.COMPLETED)")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM BookingModel b WHERE b.room.hotel.id = :hotelId AND b.status IN (com.example.BE.enums.BookingStatus.CONFIRMED, com.example.BE.enums.BookingStatus.CHECKED_IN, com.example.BE.enums.BookingStatus.CHECKED_OUT, com.example.BE.enums.BookingStatus.COMPLETED)")
    BigDecimal sumRevenueByHotel(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(DISTINCT b.user.id) FROM BookingModel b WHERE b.room.hotel.id = :hotelId")
    Long countDistinctUsersByHotel(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM BookingModel b WHERE b.checkInDate = :today OR (b.createdAt >= :startOfDay AND b.createdAt < :endOfDay)")
    Long countTodayBookings(@Param("today") LocalDate today, @Param("startOfDay") java.time.Instant startOfDay, @Param("endOfDay") java.time.Instant endOfDay);

    @Query("SELECT COUNT(b) FROM BookingModel b WHERE b.room.hotel.id = :hotelId AND (b.checkInDate = :today OR (b.createdAt >= :startOfDay AND b.createdAt < :endOfDay))")
    Long countTodayBookingsByHotel(@Param("hotelId") Long hotelId, @Param("today") LocalDate today, @Param("startOfDay") java.time.Instant startOfDay, @Param("endOfDay") java.time.Instant endOfDay);

    // Tìm các đơn đặt phòng PENDING đã quá hạn thanh toán
    @Query("""
        SELECT b FROM BookingModel b
        WHERE b.status = com.example.BE.enums.BookingStatus.PENDING
        AND (b.paymentStatus = com.example.BE.enums.PaymentStatus.UNPAID OR b.paymentStatus IS NULL)
        AND (
            (b.paymentDeadline IS NOT NULL AND b.paymentDeadline < :now)
            OR (b.checkInDate < :today)
        )
    """)
    List<BookingModel> findOverduePendingBookings(@Param("now") Instant now, @Param("today") LocalDate today);

    // Tìm các đơn CONFIRMED nhưng khách không đến sau ngày check-in và chưa check-in
    @Query("""
        SELECT b FROM BookingModel b
        WHERE b.status = com.example.BE.enums.BookingStatus.CONFIRMED
        AND b.stayStatus = com.example.BE.enums.StayStatus.NOT_CHECKED_IN
        AND b.checkInDate < :today
    """)
    List<BookingModel> findNoShowBookings(@Param("today") LocalDate today);

    // Kiểm tra xem phòng có lượt lưu trú đang CHECKED_IN không (loại trừ booking đang thao tác)
    @Query("""
        SELECT COUNT(b) > 0 FROM BookingModel b
        WHERE b.room.id = :roomId
        AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId)
        AND b.stayStatus = com.example.BE.enums.StayStatus.CHECKED_IN
        AND b.status NOT IN (com.example.BE.enums.BookingStatus.CANCELLED, com.example.BE.enums.BookingStatus.CHECKED_OUT, com.example.BE.enums.BookingStatus.EXPIRED)
    """)
    boolean hasActiveCheckedInStayOnRoom(@Param("roomId") Long roomId, @Param("excludeBookingId") Long excludeBookingId);
}
