package com.example.BE.dto.user.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull(message = "Mã phòng (roomId) không được để trống")
        Long roomId,

        @NotNull(message = "Ngày nhận phòng (checkInDate) không được để trống")
        @FutureOrPresent(message = "Ngày nhận phòng không thể ở trong quá khứ")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate checkInDate,

        @NotNull(message = "Ngày trả phòng (checkOutDate) không được để trống")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate checkOutDate
) {
}
