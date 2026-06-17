package com.example.BE.dto.user.response;

import com.example.BE.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private Long userId;
    private String username;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}





