package com.example.BE.repository;
import com.example.BE.model.BookingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


@Repository
public interface BookingRepository extends JpaRepository<BookingModel, Long> {
    @Query("""
        SELECT COUNT(b) > 0
        FROM BookingModel b
        WHERE b.room.id = :roomId
        AND b.status <> com.example.BE.enums.BookingStatus.CANCELLED
        AND b.checkInDate < :checkOutDate
        AND b.checkOutDate > :checkInDate
    """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

}

