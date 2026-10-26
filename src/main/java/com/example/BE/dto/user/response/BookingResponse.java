package com.example.BE.dto.user.response;

import com.example.BE.enums.BookingStatus;
import com.example.BE.enums.PaymentStatus;
import com.example.BE.enums.StayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long id;
    private Long roomId;
    private String roomNumber;
    private String roomName;
    private String roomType;
    private Long hotelId;
    private String hotelName;
    private Long userId;
    private String username;
    private String customerName;
    private String email;
    private String phone;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal totalPrice;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private BigDecimal extraCharges;
    private BookingStatus status;
    private StayStatus stayStatus;
    private PaymentStatus paymentStatus;
    private Instant paymentDeadline;
    private Instant actualCheckInTime;
    private Instant actualCheckOutTime;
    private String checkedInBy;
    private String checkedOutBy;
    private Instant createdAt;
    private Instant updatedAt;
}
