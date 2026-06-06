package com.example.BE.enums;

import lombok.Getter;

@Getter
public enum BookingStatus {
    PENDING("Chờ thanh toán/Xác nhận"),
    CONFIRMED("Đã xác nhận"),
    CANCELLED("Đã hủy"),
    CHECKED_IN("Đã nhận phòng"),
    CHECKED_OUT("Đã hủy phòng");
    private final String displayValue;
    BookingStatus(String displayValue) {
        this.displayValue = displayValue;

    }
}
