package com.example.BE.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillPreviewResponse {
    private Long bookingId;
    private String guestName;
    private String roomNumber;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long actualNights;
    private BigDecimal roomRatePerNight;
    private BigDecimal totalRoomCharge;
    private BigDecimal extraCharges;
    private BigDecimal totalBill;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private boolean fullyPaid;
}
