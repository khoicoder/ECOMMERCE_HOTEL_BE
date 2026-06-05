//package com.example.BE.repository;
//
//import com.example.BE.model.BookingModel;
//import com.example.BE.model.BookingStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//
//public interface BookingRepository extends JpaRepository<BookingModel,Long> {
//
//    long count();
//    long countByStatus(BookingStatus status);
//    @Query("""
//        select count(b) from Booking b WHERE b.createdAt between :start and :end
//"""
//     )long countTodayBookings(
//             Instant start,
//             Instant end
//    );
//
//    @Query("""
//    select coalesce(sum(b.totalPrice),0)
//    from Booking b
//    where b.status = 'CONFIRMED'
//""")
//    BigDecimal getTotalRevenue();
//}
