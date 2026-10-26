package com.example.BE.enums;

import lombok.Getter;

@Getter
public enum BookingStatus {
    PENDING("Chờ thanh toán/Xác nhận"),
    CONFIRMED("Đã xác nhận/Giữ chỗ"),
    CANCELLED("Đã hủy"),
    EXPIRED("Hết hạn thanh toán"),
    NO_SHOW("Khách không đến"),
    CHECKED_IN("Đã nhận phòng"),
    CHECKED_OUT("Đã trả phòng"),
    COMPLETED("Hoàn tất chu trình");

    private final String displayValue;

    BookingStatus(String displayValue) {
        this.displayValue = displayValue;
    }
}
