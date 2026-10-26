package com.example.BE.repository;

import com.example.BE.model.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentModel, Long> {
    List<PaymentModel> findByBooking_IdOrderByPaidAtDesc(Long bookingId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentModel p WHERE p.booking.id = :bookingId AND p.paymentStatus = com.example.BE.enums.PaymentStatus.SUCCESS")
    BigDecimal sumPaidAmountByBookingId(@Param("bookingId") Long bookingId);

    boolean existsByTransactionCode(String transactionCode);
}
